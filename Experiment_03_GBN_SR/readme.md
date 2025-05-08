在中文汉化版的 IntelliJ IDEA 中，对应的菜单和选项如下：
1. **打开“运行”→“编辑配置”窗口**

    * 右上角运行按钮旁边的小下拉箭头，点击它
    * 选择 **“编辑配置”**（ **Edit Configurations…**）

2. **添加新的运行/调试配置**

    * 在左上角的配置列表上方，点击 **“+”**
    * 在弹出的列表里选 **“应用程序”**（**Application**）

3. **配置接收端（Receiver）**

    * **名称（Name）**：Receiver
    * **模块（Module）**：Experiment_03_GBN_SR
    * **主类（Main class）**：Main
    * **程序参数（Program arguments）**：recv 8888
    * **工作目录（Working directory）**：保持默认即可

4. **再添加一条发送端（Sender）配置**

    * 重复上面“添加新的运行/调试配置”的步骤，选择 **“应用程序”**
    * **名称（Name）**：Sender
    * **模块（Module）**：Experiment_03_GBN_SR
    * **主类（Main class）**：Main
    * **程序参数（Program arguments）**：send 127.0.0.1 8888 Hello\_GBN\_SR\_Test
    * **工作目录（Working directory）**：依然保持默认

配置完成后，左上角的下拉列表里就会出现 “Receiver” 和 “Sender” 两条配置，分别选中对应项点击 ▶️ 按钮就能启动接收端或发送端了。
