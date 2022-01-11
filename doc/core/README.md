# Core

<br>

---

<br>

Core 裡面一共只有 2 個 Interface：

<br>

* `Monitor`

* `RecordReader`

<br>
<br>

## `Monitor` 介面

<br>

這個介面定義了一個 `Monitor` 需要實現的所有功能：

<br>

```java
public interface Monitor {

    // 初始化動作進行。
    void init();

    void startup();

    void restart();

    // 清除已經不存在的 log 壓縮檔
    void cleanPersistence();

    // 預掃描，在正式啟動前先掃描一次資料夾，把掃描到的現有文件 metadata 添加進 db 中
    void prescan() throws IOException;

    // 補齊先前的資料
    void completePreviousData();

    void close();

    MonitorProperties getProperties();
}
```

<br>
<br>

## `RecordReader`

<br>

這是一個 FunctionalInterface，只有一個必須被實現的方法：

<br>

```java
@FunctionalInterface
public interface RecordReader<T> {

    void readRecord(T t);

    default void flush() {
        return;
    }
}
```

<br>

`readRecord()` 方法定義如何處裡新產生的 record 紀錄。


