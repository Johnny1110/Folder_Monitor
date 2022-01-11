# Utils

<br>

---

<br>

這個 package 存放會使用到的 Utils，其是大部分的檔案處理邏輯都放在這裡。

<br>

## MonitorUtils

<br>

這個類別裡面都是 Monitor 邏輯相關的處裡工具。在 Monitor 的實作中盡量只設計處裡流程，把每一個流程的細節處理部分移植到這邊，這樣一來之後要調整流程的話才比較不會不停的改到細節。

<br>

下面介紹一下每個方法：

<br>
<br>

### `List<FolderFile> scanAllfile(String monitorName, Pattern datePattern, SimpleDateFormat sdf, Instant cutOffDate, String folderPath, String filenamePrefix, String filenameSurfix)`

<br>

這個方法是掃描全部檔案，他需要的參數如上面所示。

在這個方法中，使用到 `Files.walk()` 方法歷遍目標目錄下的所有檔案。當發現歷到的檔案是 `zip` 或 `.tar.gz` 的話會有一些特殊處理分別會交由下面兩個方法處裡壓縮檔：

<br>

* `processZipFile()`

<br>

* `processTarGzFile()`

<br>

這兩個方法會在後面詳細講解，當發現歷到的檔案是一般檔按時，則交給處理單檔案的方法處裡：

<br>

* `processNormalFile()`

<br>

掃描完全部檔案後，把符合規範的檔案整理成一個 `List` 回傳。

<br>
<br>
<br>
<br>

### `FolderFile processNormalFile(String monitorName, Path absPath, SimpleDateFormat sdf, Pattern datePattern, Instant cutOffDate, String filenamePrefix, String filenameSurfix)` 

<br>

這個方法用來處理一般檔案。這邊介紹一下是如何處裡這種一般檔案的。

首先拿到檔案後先檢查檔名符不符合前後綴與正則表達式，如果都符合檢查檔案時間是否在 `cutOffDate` 之後，如果以上條件都符合則會為這個檔案建立 Entity （`FolderFile`），把資料填入這個實體類後回傳。

<br>
<br>
<br>
<br>

### `void processTarGzFile(String monitorName, Path absPath, SimpleDateFormat sdf, Pattern datePattern, Instant cutOffDate, String filenamePrefix, String filenameSurfix)`

<br>

未實作

<br>
<br>
<br>
<br>

### `List<FolderFile> processZipFile(String monitorName, Path absPath, SimpleDateFormat sdf, Pattern datePattern, Instant cutOffDate, String filenamePrefix, String filenameSurfix)`

<br>

處理 zip 壓縮檔的方法。這個方法會使用到一些 zip 專用的套件，比如：`ZipFile` `ZipInputStream` `ZipEntry`。具體細節不會多介紹，這裡只講邏輯如何處理。在這個方法中會歷遍這個壓縮檔內所有的資料然後剩下的工作就跟處理一般檔案時的邏輯一樣，最後整理成一個 `List<FolderFile>` 回傳。

<br>
<br>
<br>
<br>

### `String md5HashCode(Path absPath)` / `md5HashCode(InputStream is)`

<br>

對檔案進行 MD5 加密，回傳加密資料。

<br>
<br>
<br>
<br>

### `void compareAndFillRead(String folderPath, FolderFile file, MonitorWorkLog workLog, RecordReader<String> reader)`

<br>

讀檔方法這個方法分讀一般檔案或壓縮檔。分別交由下面 2 個方法處理：

<br>

* `readNormalFile(folderPath, file, workLog, reader);`

* `readCompressedFile(folderPath, file, workLog, reader);`

<br>
<br>
<br>
<br>

### `void readNormalFile(String folderPath, FolderFile file, MonitorWorkLog workLog, RecordReader<String> reader)`

<br>

讀取一般檔案的方法，比對實際檔案與 db 內的 hashcode 是否不一致，如果發現不一致就說明檔案有改動。根據 db 內所存的 `readline` 資料決定從哪一行開始讀取。獨到最後一行停止。


讀取完成後還要把讀取後的新行數與 hashcode 更新到 db 中。

<br>
<br>
<br>
<br>

`void readCompressedFile(String folderPath, FolderFile file, MonitorWorkLog workLog, RecordReader<String> reader)`

<br>

讀取壓縮檔的方法。如果要追朔的日期太久遠，檔案被封存進壓縮檔就需要這個方法。這邊會分成讀 zip 或 GZ 檔，分別由 2 個方法處理。

*  `readZipFile(cfpath, file, workLog, reader);`

* `readGzFile(cfpath, file, workLog, reader);`

<br>
<br>
<br>
<br>


### `void readZipFile(Path zipPath, FolderFile file, MonitorWorkLog workLog, RecordReader<String> reader)`

<br>

讀取壓縮檔資料，這邊其實跟讀單檔的處理邏輯大致一樣，不同點在於這個方法要讀取 zip 檔內的所有資料，相比讀取單檔只多了一個迴圈去歷遍檔案而已，實際讀取每一個檔案邏輯都跟 `readNormalFile()` 一樣。

<br>
<br>
<br>
<br>

### `readGzFile(Path cfpath, FolderFile file, MonitorWorkLog workLog, RecordReader<String> reader)`

<br>

未實作

<br>
<br>
<br>
<br>

### `int getTotalLines(Path absPath)`

<br>

取得文件總行數，輸入文件路徑回傳共有幾行。

<br>
<br>
<br>
<br>

### `int getZipInnerFileTotalLines(Path zipPath, FolderFile file)`

<br>

如果需要取得 zip 檔內某個文件的總行數就需要使用這個方法。