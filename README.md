# Logback Study

## 目錄

- [簡介](#簡介)
- [設定](#設定)
    - [Logback Tag](#logback-tag)
        - [configuration](#configuration)
        - [property](#property)
        - [appender](#appender)
            - [常用 appender class 額外設定](#常用-appender-class-額外設定)
        - [logger](#logger)
        - [root](#root)
    - [Spring 擴充 tag](#spring-擴充-tag)
    - [log 事件層級](#log-事件層級)
    - [Layouts pattern](#layouts-pattern)
    - [格式調整](#格式調整)

---

###### 參考資料
* [LogBack 官方文件](http://logback.qos.ch/manual/index.html)
* [SLF4J 官方文件](http://www.slf4j.org/docs.html)
* [Spring 官方文件 Spring Boot Features - Custom Log Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/spring-boot-features.html#boot-features-custom-log-configuration)
* [Springframework Guru - Using Logback with Spring Boot](https://springframework.guru/using-logback-spring-boot/)

## 簡介

## 設定
* 產生檔案 [`logback-spring.xml`](src/main/resources/logback-spring.xml)，並於加入打包設定。

    >[**pom.xml**](pom.xml)
    >```xml
    ><resources>
    >    <resource>
    >        <directory>src/main/logging</directory>
    >        <includes>
    >            <include>**</include>
    >        </includes>
    >    </resource>
    ></resources>
    >```

    * 檔案名稱用 `logback-spring.xml` 或是 `logback.xml` 都可以，官方建議使用 _"-spring"_ 以避免 Spring 沒辦法完全控制 log 的初始化。

### Logback tag
#### configuration
* `debug="true | false"`:  是否記錄產生程式啟動時，log 設定檔進行設定時的狀態資訊。  
  須符合以下兩點才會有效果：
    * 找得到設定檔。
    * 設定檔是格式良好的 xml。
* `scan="true | false"`:  是否掃描設定檔有無被改動並重新載入。
* `scapPeriod="60 seconds"`:  掃描頻率，強烈建議加上時間單位，因為預設是 millisecond。
* ***Logback 自動掃描與 Spring Boot 的相容性問題***
    * **Spring 擴充 tag _(springProfile & springProperty)_ 在自動掃描重新載入設定時會發生錯誤**<br/>
      由於 Spring Boot 是利用實作 `JoranConfigurator` _(ch.qos.logback.classic.joran.JoranConfigurator)_ 的 `SpringBootJoranConfigurator` _(org.springframework.boot.logging.logback.SpringBootJoranConfigurator)_ 來支援 Spring 擴充 tag ，不幸的是 Logback 用於自動載入的 `ReconfigureOnChangeTask` _(ch.qos.logback.classic.joran.ReconfigureOnChangeTask)_ 並未對此提供支援，初始化時由 Spring Boot 設定沒有問題，可是重新載入是由 Logback 設定就會發生錯誤，Logback 並不會認得 Spring 擴充 tag 及其以下的所有 tag。

#### property
  設置變數，並在設定檔內用 `${KEY}` 來取得 `VALUE`

* `scope="local (Default) | context | system"`: 參數存取範圍。
    * 優先順序: local > context > system
* `name="{KEY}"`: 設定變數鍵值。
* `value="{VALUE}"`: 設定變數值。

#### appender  
  設定log的輸出方式

* `name="{APPENDER_NAME}"`: 供 `logger`、`root` 參考的名稱。
* `class="{APPENDER_CLASS}"`: 不同輸出方式使用的class。
* `encoder` tag: 設定 log 的格式和編碼。
    * `patter`: 格式，詳見下方 **[Layouts pattern](#layouts-pattern)**。
    * `charset`: 編碼。
* `filter` tag: 設定過濾輸出的事件。
    * `class={FILTER_CLASS}`: 不同過濾方式的class。
    * `level`: 過濾目標。
    * `onMatch`: 符合過濾目標的反應方式。 [ACCEPT | NEUTRAL(default) | DENY]
    * `onMismatch`: 不符合過濾目標的反應方式。 [ACCEPT | NEUTRAL(default) | DENY]

##### 常用 appender class 額外設定
* **ConsoleAppender**
    * `target`: 選擇 System output。 [System.out(default) | System.err]
* **FileAppender**
    * `append`: 是否以附加的方式寫入 log 。 [true(default) | false]
    * `file`: 寫入 log 的目標檔案。
* **RollingFileAppender**
    * `append`、`file`: 同上 **FileAppender**。
    * `rollingPolicy`: 產生新 log 檔案的方式。
        * `class={ROLLING_POLICY_CLASS}`  
            * `TimeBasedRollingPolicy`: 以時間為準產生新 log 檔案。
                * `fileNamePattern`: 檔案命名格式。 ex: _`lb.%d{yyyy-MM-dd}.log`_
                * `maxHistory`: log 保留期限。以月為單位。超過會自動清除最舊的 log 檔案。
                * `totalSizeCap`: log 最大保留數量。超過會自動清除最舊的 log 檔案。如有設置 `maxHistory` 則以 `maxHistory` 為優先。
            * `FixedWindowRollingPolicy`: 以 `triggeringPolicy` 為準產生新 log 檔案，故須設置 `triggeringPolicy` 。
                * `fileNamePattern`: 檔案命名格式。 ex: _`lb.%i.log`_
                * `minIndex`: 起始索引。
                * `maxIndex`: 最大索引，設置過大會自動更改為20，超過最大索引會從最大索引開始刪除 log 檔案。
            * `SizeAndTimeBasedRollingPolicy`: 上述兩者的混用，同時以時間和以檔案大小為規則的 `triggeringPolicy` 為準產生新 log 檔案，故須設置 `triggeringPolicy` 。
    * `triggeringPolicy`: 觸發產生新 log 檔案的時機。
        * `SizeBasedTriggeringPolicy`: 超過最大設置檔案大小時，觸發產生新 log 檔案。
            * `maxFileSize`: 最大檔案大小。可用單位：[KB | MB | GB]，不輸入單位則為 bytes 。預設：10MB。

#### logger
  控制整個 packages 或單個 class 內的 log 輸出設定。  
  多個相同 logger tag 會依設定檔中的順序覆蓋 `level`、`additivity` 的設定，`appender-ref` 則會疊加。

* `name="PACKAGE | CLASS"`: 設定要控制的 packages 或 class。
* `level="TRACE | DEBUG | INFO | WARN | ERROR | OFF | ALL | INHERITED(= NULL)"`: 設定輸出的事件層級。
* `additivity="true | false"`: 是否也輸出到上層 logger ，會無視上層 logger 的輸出 `level` 。
* `appender-ref` tag: 設定輸出方式，可設定零個到多個。
    * `ref`: 設定 appender 的名稱。

#### root
  控制 root logger 的輸出設定

* `level="TRACE | DEBUG (Default) | INFO | WARN | ERROR | OFF | ALL"`: 設定輸出的事件層級。
* `appender-ref` tag: 同上。

### Spring 擴充 tag
* **springProperty**  
  設置環境變數，類似 `property` tag。
    * `scope="local (Default) | context | system"`: 同上 property 。
    * `name="{KEY}"`: 設定變數鍵值。
    * `source="{PROPERTY_KEY}"`: 設定檔中的鍵值。
    * `defaultValue="{DEFAULT_VALUE}"`: 變數預設值。
* **springProfile**  
  把 `logger` 和 `root` tag 放到這個 tag 下，即可依照運行的 profile 控制 log 輸出。  
    * `name="PROFILE_NAME"`: profile 名稱。

### log 事件層級

|紀錄層級&rarr;<br/>請求層級&darr;&nbsp;&nbsp;|TRACE|DEBUG|INFO|WARN|ERROR|OFF|
|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|TRACE|**YES**|NO|NO|NO|NO|NO|
|DEBUG|**YES**|**YES**|NO|NO|NO|NO|
|INFO|**YES**|**YES**|**YES**|NO|NO|NO|
|WARN|**YES**|**YES**|**YES**|**YES**|NO|NO|
|ERROR|**YES**|**YES**|**YES**|**YES**|**YES**|NO|

### Layouts pattern
  _設定pattern時，關鍵字皆以 **`%`** 開頭；如須做為符號使用，要用 **`\`** 跳過。_

|關鍵字|敘述|
|--:|:--|
|**p**<br/>**le**<br/>**level**|logger記錄的事件層級|
|**c**{_length_}<br/>**lo**{_length_}<br/>**logger**{_length_}|發出記錄請求的logger 名稱。<br/>_length_: 顯示長度。超過的話會從左開始不論長短略縮package 名稱。 0 代表不顯示packages。|
|**C**{_length_}<br/>**class**{_length_}|發出記錄請求的class 名稱。<br/>_length_: 同上。|
|**t**<br/>**thread**|發出記錄請求的執行緒名稱。|
|**L**<br/>**line**|記錄請求的行數|
|**d**{_pattern_}<br/>**date**{_pattern_}<br/>**d**{_pattern_,_timezone_}<br/>**date**{_pattern_,_timezone_}|_pattern_:<br/>&nbsp;&nbsp;&nbsp;&nbsp;**yyyy-MM-dd HH:mm:ss.SSS**: 年-月-日 時:分:秒.毫秒<br/>**(須注意分隔符號不得使用`,`)**|
|**m**<br/>**msg**<br/>**message**|logger記錄到的訊息|
|**n**|換行符號|
|**X**{_key:-defaultVal_}<br/>**mdc**{_key:-defaultVal_}|將`key=value`存放在MDC(mapped diagnostic context)內，供log存取使用。<br/>`:-`後接預設值。|

### 格式調整
  % **_min_._MAX_** Keyword | **(**Pattern**)**

* 由於 log 排版只靠 layouts pattern 可能會有點亂，可以在關鍵字加入 min.max 協助排版。  
  若是要調整一段pattern，則須要在pattern前後加上括號 `()`。

* min 和 max 之間以一個小數點 **`.`** 隔開，且各自帶上負號會有不同意義。
    * min: 最小長度，不足將以空格補足，預設為往左補空格，帶上負號表示往右補。
    * max: 最大長度，超過將裁切字元，預設為從左邊開始切字元，帶上負號表示從右邊切。

* 範例

    ||最小長度|最大長度|logger 名稱|輸出|
    |--:|:-:|:-:|:--|:--|
    |[%20logger]|20|-|packages.logger|[&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;packages.logger]|
    |[%-20logger]|20|-|packages.logger|[packages.logger&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;]|
    |%.10logger|-|10|packages.logger|ges.logger|
    |%.-10logger|-|10|packages.logger|packages.l|
    |%10.20logger|10|20|com.example.sbs.logger|m.example.sbs.logger|
    |%10.-20logger|10|20|com.example.sbs.logger|com.example.sbs.logg|