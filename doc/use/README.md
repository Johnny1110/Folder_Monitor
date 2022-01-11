# 使用

<br>

---

<br>

設定餐數與啟動：

<br>

```java
MonitorProperties properties = new MonitorProperties();
properties.setMonitorName("name");
properties.setDateFormate("yyyy-MM-dd");
properties.setCutOffDate(LocalDate.of(2020, 4, 3).atStartOfDay(ZoneId.systemDefault()).toInstant());
properties.setDateRegex("[0-9][0-9][0-9][0-9]-[0-1][0-9]-[0-3][0-9]");
properties.setFilenamePrefix("dmdebug");
properties.setFilenameSurfix(".log");
properties.setFolderPath("D:\\logs");
properties.setReadPrevious(true);
properties.setJdbcUrl("jdbc:sqlite:D:\\code\\log_monitor\\cfg\\test.db");
properties.setJdbcUsername(null);
properties.setJdbcPassword(null);

LogMonitor monitor = new LogMonitor(properties, System.out::println);

Monitor monitorAgent = new LogMonitorProxy(monitor);

monitorAgent.init();
monitorAgent.prescan();
monitorAgent.cleanPersistence();
monitorAgent.completePreviousData();
monitorAgent.startup();
```

<br>

在啟動上需要設定一些必要的參數。這些參數都存放在 `MonitorProperties` 這個 java Bean 中。

<br>

* `MonitorName`：是這一整個監控工作的名稱，作為 PK 存放進 DB 中。

<br>

* `DateFormate`：檔案異動日期格式，存入 DB 就會依照這個設定來做。

<br>

* `cutOffDate`：cutOffDate 是忽略哪個時間點之前的資料，止處裡這個時間點之後的資料。

<br>

* `dateRegex`：使用正則表達式，當這個參數被設定好後，就會按照這個格式去目標目錄內掃描，只有符合這個格式的檔案名稱才會被這隻程式注意到。

<br>

* `filenamePrefix`：結合 `dateRegex` 與 `filenameSurfix`，這三個組合起來就是一個完整的檔案名稱。Prefix 是名稱前綴。

<br>

* `filenameSurfix`：結合 `dateRegex` 與 `filenameSurfix`，這三個組合起來就是一個完整的檔案名稱。Prefix 是名稱後綴。

<br>

* `folderPath`：要監控的目錄絕對位置。

<br>

* `readPrevious`：是否掃描之前的紀錄，因為有可能程式啟動前，log 已經更新了一段時間了，這個參數設定為 `true` 的話，會把之前的紀錄補上。（從上次最後一個讀取紀錄點）

<br>

* `jdbcUrl`：字面上的意思，就是 jdbc url。

<br>

* `jdbcUsername`：db user 名稱。

<br>

* `jdbcPassword`：db user 密碼。

<br>
<br>

建立 `LogMonitor` 物件，這個物件是真正在處裡監控任務的核心。這之後會介紹。

<br>

```java
LogMonitor monitor = new LogMonitor(properties, System.out::println);
```

<br>

`System.out::println` 暫時用來替代 RecordReader，把讀取到的 log 直接印在 console。

<br>

另外為了要加入一些即時的應用 log 紀錄，所以還會加入一個 `LogMonitorProxy`，他也是實現 `Monitor` 介面的物件。我們呼叫 `Monitor` 的 func 時都會委派 `LogMonitorProxy` 代理，`LogMonitorProxy` 在委派真正的 LogMonitor 之前，他會在每一個方法前或後加入 log 功能，例如：

<br>

```java
@Override
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

可以看到在 `monitor.init()`，後面會加入一個 `log.info("")` 輸出，而且當發生錯誤時，也會由 log 印出錯誤。

<br>
<br>

一切設定好後，需要就可以啟動應用了。一共有 5 個步驟：

<br>

```java
monitorAgent.init();
monitorAgent.prescan();
monitorAgent.cleanPersistence();
monitorAgent.completePreviousData();
monitorAgent.startup();
```

<br>

* `init()`：初始化，把一些需要的物件都建立好。

* `prescan()`：預掃描，這一個階段會把目標目錄中的資料做一個掃描的動作，主要是在 db 內更新目標目錄。

* `cleanPersistence()`：實際上目錄內的資料檔案或許會被人工刪除或者定期自動刪除，這個方法就會檢查實際資料與 db 資料那些是不對起來的，如果 db 有這筆資料，而實際上沒有，那就會移除 db 中的資料。

* `completePreviousData()`：這個方法的功能是，補完先前資料，由於應用可能在運行一段時間後被關閉，之後又再次啟動，這中間的真空期資料會由這個方法功能補齊。如果在　properties 中設定 `isReadPrevious` 屬性為 `false` 的話，這個功能會跳過不執行。

* `startup()`：啟動監控功能。

<br>
<br>

