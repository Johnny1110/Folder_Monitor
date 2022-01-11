# Listener

<br>

---

<br>

這裡只有一個 `FileAlterationListenerAdaptor` 的子類別 `LogFileListener`。當檔案發生任何異動時的對應處裡邏輯都在這裡實現。

<br>

先看一下建構函式：

<br>

```java
public LogFileListener(String folderPath, MonitorWorkLog workLog, RecordReader<String> reader){
    this.monitorWorkLog = workLog;
    this.folderPath = folderPath;
    this.reader = reader;
}
```

<br>
<br>

最重要的是以下的兩個 `@Overwrite` 方法：

<br>

* `onFileChange(File file)`：

    再這個方法中，當發現有檔案異動，則會去 db 中找出這個檔案的相關紀錄。對比檔案目前行數與 db 中的 `readLine`(已讀) 行數，把新增的行數(未讀)行數透過 `RecordReader` 寫出。

<br>

* `onFileDelete(File file)`：

    再這個方法中，當發現有檔案被刪除，會檢查這個檔案是否式 zip 檔或者 tar.gz 檔。如果是這種壓縮檔的話，則會直接把 db 中這個壓縮檔的資料全部刪除。如果是一般檔案則不做動作，因為一般檔案消失不會直接消失。
