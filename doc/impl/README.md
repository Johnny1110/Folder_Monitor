# impl

<br>

---

<br>

這裡是 `Monitor` 實作類的 package。最主要的就是 3 個 class :


<br>

* `LogMonitor`

* `LogMonitorProxy`

* `MonitorProperties`

<br>
<br>

## LogMonitor

<br>

`LogMonitor` 實現了 `Monitor` 介面，建構式如下：

<br>

```java
public LogMonitor(MonitorProperties properties, RecordReader<String> reader){
    this.properties = properties;
    this.recordReader = reader;
}
```

<br>
<br>
<br>
<br>

### `init()` 方法：

<br>

初始化方法工作有點多，這裡把說明寫在註解裡面：

<br>

```java
@Override
public void init() {
    // 建立 JDBCMonitorWorkLog，這個類別是負責持久化工作
    monitorWorkLog = new SqliteJDBCMonitorWorkLog();
    monitorWorkLog.initConnection(properties.getJdbcUrl(), properties.getJdbcUsername(), properties.getJdbcPassword());

    // 設定 dataPattern，由 properties 取出。
    this.datePattern = Pattern.compile(properties.getDateRegex());

    // 設定存入 db 的日期格式。
    this.simpleDateFormat = new SimpleDateFormat(properties.getDateFormate());

    // MonitorJob 是一個 Java Bean，存放一個監管工作所需的所有屬性。
    MonitorJob job = new MonitorJob();
    job.setMonitorName(properties.getMonitorName());
    job.setRegexStr(properties.getDateRegex());
    job.setFilenameSurfix(properties.getFilenameSurfix());
    job.setFilenamePrefix(properties.getFilenamePrefix());
    job.setFolderPath(properties.getFolderPath());

    // 在 db 中建立這個 job 的資料，如果已存在則不會重複建立
    monitorWorkLog.createMonitorJob(job);

    // 從 db 中取出這個 job 相關資料。
    this.monitorJob = monitorWorkLog.getMonitorJobByName(properties.getMonitorName());
    logger.info("monitor_name info from DB : " + monitorJob);
}
```

<br>
<br>
<br>
<br>

`startup()` 方法：

<br>

啟動方法，這裡用到 `apache-commons` 套件。幫助我們實現監管目錄功能。

<br>

```java
public void startup() {
    // 第一個 IOFileFilter 設定為針對檔案的 prefix 與 surfix 的過濾器。
    IOFileFilter filefilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
            FileFilterUtils.suffixFileFilter(monitorJob.getFilenameSurfix()),
            FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
    );

    // 第二個 IOFileFilter 設定為專門針對壓縮檔的 prefix 與 surfix 過濾器。
    IOFileFilter compFilters = FileFilterUtils.and(FileFilterUtils.fileFileFilter(),
            FileFilterUtils.suffixFileFilter(".zip"),
            FileFilterUtils.prefixFileFilter(monitorJob.getFilenamePrefix())
    );

    // 合併兩個過濾器
    IOFileFilter finalFilter = FileFilterUtils.or(filefilters, compFilters);

    // 建立 FileAlterationObserver 物件，輸入目標目錄與上面建立好的 IOFileFilter 作為建構參數。
    FileAlterationObserver observer = new FileAlterationObserver(monitorJob.getFolderPath(), finalFilter);

    // 加入 Listener 實現在檔案 `Change` 與 `Delete` 階段插入處裡邏輯。
    observer.addListener(new LogFileListener(monitorJob.getFolderPath(), monitorWorkLog, recordReader));

    // 建立 FileAlterationMonitor 物件。
    monitor = new FileAlterationMonitor(properties.getInterval(), observer);
    try {
        // 啟動。
        monitor.start();
    } catch (Exception e) {
        logger.info("failed to startup monitor.", e);
    }
}
```

<br>
<br>
<br>
<br>

`restart()` 方法：

<br>

restart 的步驟：

<br>

```java
close();
init();
cleanPersistence();
startup();
```

<br>
<br>
<br>
<br>

`cleanPersistence()` 方法：

<br>

清理 db 內已經不需要(實際檔案已消失)的紀錄。

<br>

```java
public void cleanPersistence() {
    // 找出所有的 parentPackNames
    List<String> parentPackNames = monitorWorkLog.findAllParentPackNameByMonitorName(monitorJob.getMonitorName());
    // 巡遍目標目錄的資料夾，對照 db 的 parentPackName，把已經不存在的目錄名稱蒐集起來。
    List<String> needRemove = parentPackNames
            .stream()
            .filter(name -> {
                Path ppath = Paths.get(monitorJob.getFolderPath(), name);
                return Files.notExists(ppath);})
            .collect(Collectors.toList());

    // 刪除 db 中這些不存在實際目錄的紀錄。
    monitorWorkLog.deleteFolderFileByParentPackName(needRemove);
}
```

<br>
<br>
<br>
<br>

`prescan()` 方法：

<br>

預掃描方法。在這一個步驟中，會掃描目標目錄，並在 db 中建立起紀錄，如果發現之前的單個檔案已經因為時間的關係被壓縮成 zip 或者存成子目錄的話，就更新 `parentPackName` 欄位。

<br>
<br>
<br>
<br>

`completePreviousData()` 方法：

<br>

這個方法的作用是補輸出先前的資料，假如有一段時間應用處於關閉狀態，而目標目錄的文件(log 檔)繼續生產，那這樣一來就會有這一部份的紀錄被遺漏，使用此方法就可以補齊先前遺漏的資料直到當前最新文件行數。

<br>
<br>

`close()` 方法：

<br>

關閉 `monitor` 與 `monitorWorkLog`（JDBC）

<br>
<br>
<br>
<br>

`getProperties()` 方法：

<br>

取出屬性檔，可以查看內容。

<br>
<br>
<br>
<br>

## LogMonitorProxy

<br>

這個類別實作了 Monitor 介面，因為要實現這個應用的 log 功能，又不想把
 log 功能跟主邏輯混在一起，所以使用代理設計模式來處理 log。這邊不會全部說明 code 細節。指取一段說明：

 <br>

```java
private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);

private MonitorProperties properties;

private LogMonitor monitor;

public LogMonitorProxy(LogMonitor monitor){
    this.monitor = monitor;
    this.properties = monitor.getProperties();
}

public void init() {
    try{
        monitor.init();
        logger.info("monitor inited.");
    }catch (Exception ex){
        logger.error("failed to init Monitor.", ex);
    }
}
```

<br>

可以看到，呼叫 `init()` 方法後，先把請求移交給真正的 `LogMonitor` 去處理，然後再處理 `logger` 邏輯 。

<br>
<br>
<br>
<br>

## MonitorProperties

<br>

這個類別是純粹的 Java Bean，存放一些必要的屬性。

<br>

`boolean readPrevious`：是否掃描之前的紀錄，因為有可能程式啟動前，log 已經更新了一段時間了，這個參數設定為 `true` 的話，會把之前的紀錄補上。（從上次最後一個讀取紀錄點）

<br>

`String dateRegex`：使用正則表達式，當這個參數被設定好後，就會按照這個格式去目標目錄內掃描，只有符合這個格式的檔案名稱才會被這隻程式注意到。

<br>

`String monitorName`：是這一整個監控工作的名稱，作為 PK 存放進 DB 中。

<br>

`String folderPath`：要監控的目錄絕對位置。

<br>

`String filenamePrefix`：結合 `dateRegex` 與 `filenameSurfix`，這三個組合起來就是一個完整的檔案名稱。Prefix 是名稱前綴。

<br>

`String filenameSurfix`：結合 `dateRegex` 與 `filenameSurfix`，這三個組合起來就是一個完整的檔案名稱。Prefix 是名稱後綴。

<br>

`Instant cutOffDate`：cutOffDate 是忽略哪個時間點之前的資料，止處裡這個時間點之後的資料。

<br>

`String dateFormate`：檔案異動日期格式，存入 DB 就會依照這個設定來做。

<br>

`long interval`：建立　`FileAlterationMonitor` 需要的建構參數，間格多少秒掃描一次異動狀況。(非全部掃描，指掃異動過的)

<br>

`String jdbcUrl`：字面上的意思，就是 jdbc url。

<br>

`String jdbcUsername`：db user 名稱。

<br>

`String jdbcPassword`：db user 密碼。

<br>
<br>
<br>
<br>