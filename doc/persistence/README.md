# Persistence

<br>

---

<br>

 持久層為了設計讓不同的 DBMS 都可以適用，所以設計了一個介面用一個抽象類別來實作出 JDBC 增刪改查邏輯，最後再由一個普通類別繼承這個抽象類並載入 JDBC Driver 來實現替換 DBMS 功能。這個方法事實上並不穩妥，因為不同的 DBMS 在 SQL 語法上多多少少還是有不同的。如果某個 DBMS 的 SQL 差異過大就無法實現這樣的操作。其實使用如 Hibernate 等 ORM 持久層框架可以更好的處裡這類需求，之後如果有必要的話可以考慮換一下 ORM 框架。

<br>
<br>
<br>
<br>

## Entity

<br>

實體類一共指有 2 個，分別對應 db_schema 中提到的 2 張 table：

<br>

### `MonitorJob`：

<br>

```java
private String monitorName;

private String folderPath;

private Instant createdAt;

private String filenamePrefix;

private String filenameSurfix;

private String regexStr;

//getter() & setter()
```


<br>
<br>

### `FolderFile`：

<br>

```java
public class FolderFile implements Serializable, Comparable<FolderFile>{

    private String fileName;

    private int readLine;

    private String hashCode;

    private Instant createdAt;

    private Instant updatedAt;

    private String monitorName;

    private String parentPackName;

    private Instant logDate;

    //getter() & setter()

    @Override
    public int compareTo(FolderFile other) {
        return (int) (this.getLogDate().toEpochMilli() - other.getLogDate().toEpochMilli());
    }

}
```

<br>

FolderFile 實作 Comparable，因為這些 file 需要做排序。


<br>
<br>
<br>
<br>

## `MonitorWorkLog` 介面

<br>

這個介面定義了針對 DB 都要實現哪一些操作。這邊介紹一下每一個方法的作用，實作那邊就不會贅述，內容也不會多介紹，都是簡單的 JDBC 語法。

<br>
<br>

`List<FolderFile> findAllFileByMonitorName(String monitorName);`

使用 `monitorName` 找出這個監控工作所監管的所有檔案。

<br>

`List<FolderFile> findAllFileByMonitorNameAndAfterInstant(String monitorName, Instant instant);`

使用 `monitorName` 找出這個監控工作所監管的檔案，同時把時間條件加入，要找出在 instant 時間參數之後的資料。

<br>

`MonitorJob getMonitorJobByName(String monitorName);`

使用 `monitorName` 查找出 MonitorJob 實體類的資料。

<br>

`void createMonitorJob(MonitorJob job);`

建立 MonitorJob 資料，需要輸入 `MonitorJob` 實體

<br>

`void createFolderFiles(List<FolderFile> files);`

建立檔案資料，需要輸入 `FolderFile` 陣列。

<br>

`void insertOrUpdateFolderFiles(List<FolderFile> files);`

新增或更新檔案資料，只要是 db 內不存在的都一率視為新增，如果已存在相同名稱，則是更新。

<br>

`void updateFolderFiles(List<FolderFile> files);`

更新檔案，輸入 FolderFile 陣列，把陣列中的資料都更新。

<br>

`void updateParentPackName(List<FolderFile> files);`

更新父目錄名稱，輸入 FolderFile 陣列，這個方法會把陣列中檔案的父目錄欄位做更新。

<br>

`FolderFile getFileByFilename(String filename);`

輸入 filename 查詢 FolderFile 資料。

<br>

`void deleteFolderFileByParentPackName(String parentPackName);`

輸入一個父目錄名稱，把其目錄下的檔案都刪除。

<br>

`List<String> findAllParentPackNameByMonitorName(String monitorName);`

輸入監控任務的名稱，查找出這個任務監管下的所有父目錄名稱。

<br>

`void deleteFolderFileByParentPackName(List<String> parentPackName);`

輸入父目錄名稱陣列，把這些父目錄刪除。

<br>
<br>
<br>
<br>


## `JDBCMonitorWorkLog` 介面

<br>


`JDBCMonitorWorkLog` 介面實作 `MonitorWorkLog` 介面，之後關於 `JDBC` 持久化的實現都需要實作這個介面。

<br>

在原本必須要實現的一系列增刪改查功能下，`JDBCMonitorWorkLog` 要求實作類額外實現連線 JDBC 與關閉 JDBC 的功能：

<br>

```java
void initConnection(String url, String username, String password);

void closeConnection();
```

<br>

還有一個 `default` 功能：

<br>

```java
default void loadDriver(String driverName){
    try {
        Class.forName(driverName);
    } catch (ClassNotFoundException e) {
        logger.error("failed to load jdbc driver.", e);
    }
}
```

<br>

這樣一來，子類別指需要在建構物件時直接使用 `loadDriver()` 傳入 drivername 就可以載入 jdbc 了。

<br>
<br>
<br>
<br>

## `AbstractJDBCMonitorWorkLog` 類別

<br>

`AbstractJDBCMonitorWorkLog` 實作了 `JDBCMonitorWorkLog` 介面，在這個抽象類別中，需要實現所有 `JDBCMonitorWorkLog` 的功能。

這樣一來，留給繼承這個抽象類物件的工作就只剩下載入哪一個 JDBC Driver 了，我的構想是，當需要替換不同 DBMS 的時候指需要建立一個新的 class 繼承這個類別，然後在建構式中 `loadDriver()`。

<br>

關於 JDBC 實作部分就不一一做說明。

<br>
<br>
<br>
<br>

## `SqliteJDBCMonitorWorkLog` 類別

<br>

`SqliteJDBCMonitorWorkLog` 繼承 `AbstractJDBCMonitorWorkLog`，因為幾乎所有功能都被父類別時做完了，所以做為子類 `SqliteJDBCMonitorWorkLog` 要負責的任務其事就是載入 Sqlite JDBC Driver。

<br>

```java
public class SqliteJDBCMonitorWorkLog extends AbstractJDBCMonitorWorkLog {

    private static final String DRIVER_NAME = "org.sqlite.JDBC";

    public SqliteJDBCMonitorWorkLog(){
        this.loadDriver(DRIVER_NAME);
    }
}
```

<br>
<br>


