Special thanks to JavaFX v19: https://openjfx.io/<br>
<br>
專題只要求以local host進行開發，然後我一直都沒時間去試能不能找個免費伺服器架上去之類的......所以雖然多人線上的架構跟所有遊戲功能都完成了，但都在同一台電腦上，只能作簡單演示。伺服器開啟之後，客戶端可以多開，但不要開太多。<br>
至於專題報告裡提到的「開發環境」，前幾天自學各種包裝JRE的方式，現在也不用必須靠IDE跑。但過了這麼久，Java版本已經來到17版；JavaFX也變成19版了......<br>
不過前幾天在測試時，忽然出現不定時、不定頻率「連線已被主機上的軟體終止」的問題。之前專題開發過程中及期末DEMO都完全沒有出現這個問題，都local host了還會斷線......之前電腦是Windows 10，最近換成Windows 11，不知道是不是內建防護機制的問題，又或者是其他原因，目前暫時沒空深入了解。<br>
<details><summary>展開遊戲規則</summary>
只要被其他玩家擊中一次便直接離場。<br>
收刀/上膛：<br>
將鼠標移到畫面中心，此時人物不會移動，開始收刀/上膛。如果刀已出鞘且槍也還沒上膛，則鼠標在「人物面對方向」的左方為收刀，右方為上膛，否則自動執行還沒完成的一邊。<br>
移動：<br>
鼠標移出畫面中心部分，人物會自動朝鼠標方向移動。<br>
平砍：<br>
若刀已出鞘，人物移動時點擊滑鼠左鍵，經過極短的延遲及範圍預警後，施展一次中等範圍的劈砍範圍攻擊。<br>
拔刀斬：<br>
若刀已入鞘，人物移動時點擊滑鼠左鍵，經過一段時間的延遲及範圍預警後，施展一次極大範圍的劈砍範圍攻擊，並拔刀。<br>
拔刀砍子彈：<br>
當刀入鞘時，若人物受到「狙擊」，則會立刻自動拔刀並免死。<br>
一瞬拔刀：<br>
當刀入鞘時，若有其他玩家接近至自動拔刀距離(介於平砍範圍的最大距離與拔刀斬範圍的最大距離之間)便會自動拔刀，瞬發斬殺該玩家(單體攻擊)。若兩人皆能拔刀，則互相拔刀招架，無人出場。<br>
狙擊：<br>
槍上膛後，人物移動時點擊滑鼠右鍵，經過一段時間的瞄準及範圍預警後，朝目標方向全地圖狙擊一次，此為命中第一個玩家的單體攻擊。<br>
刺刀：<br>
槍未上膛時，人物移動時點及滑鼠右鍵，瞬發進行一次極短距離的近戰單體攻擊。此攻擊為狙擊槍的刺刀，並不會拔刀。<br>
再來一次：<br>
死亡後點擊視窗，會重新連線創建新角色。<br>
關閉視窗：<br>
直接登出跳GAME，伺服器端會偵測並將該玩家移除。
</details>

「scripts」僅供參考，無法單獨編譯執行。<br>
下載方式：https://drive.google.com/drive/folders/1fOTvLb6WUhz0L9M903ZIoWgQNh4V_yCO?usp=sharing<br>
直接下載「jre.zip」，解壓縮後執行「server(windows).bat」及「client(windows).bat」。<br>
其他平台可嘗試自行修改bat檔指令內容，但程式未經過其他平台測試，僅理論上可運行。<br>
「project.zip」為完整Eclipse project資料夾。
