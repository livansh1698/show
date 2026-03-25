using System;
using System.IO;
using System.IO.Compression;
using System.Diagnostics;
using System.Reflection;
using System.Windows.Forms;

class Program {
    [STAThread]
    static void Main() {
        try {
            string tempDir = Path.Combine(Path.GetTempPath(), "Show_Portable_App");
            string exePath = Path.Combine(tempDir, @"Show\Show.exe");
            
            string currentExe = Assembly.GetExecutingAssembly().Location;
            DateTime currentExeTime = File.GetLastWriteTime(currentExe);
            bool shouldExtract = true;

            // 智能缓存：如果解压出的文件较旧或不存在，则重新解压
            if (File.Exists(exePath)) {
                DateTime extractedExeTime = File.GetLastWriteTime(exePath);
                // 允许 5 秒误差
                if (extractedExeTime >= currentExeTime.AddSeconds(-5)) {
                    shouldExtract = false;
                }
            }

            if (shouldExtract) {
                if (Directory.Exists(tempDir)) {
                    Directory.Delete(tempDir, true);
                }
                Directory.CreateDirectory(tempDir);
                
                // 从嵌入的资源中读取 ZIP
                using (Stream stream = Assembly.GetExecutingAssembly().GetManifestResourceStream("Show.zip")) {
                    if (stream == null) {
                        MessageBox.Show("内部数据丢失: 找不到资源 Show.zip", "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
                        return;
                    }
                    string zipPath = Path.Combine(tempDir, "app_data_temp.zip");
                    using (FileStream fs = new FileStream(zipPath, FileMode.Create)) {
                        stream.CopyTo(fs);
                    }
                    // 解压到 TEMP
                    ZipFile.ExtractToDirectory(zipPath, tempDir);
                    File.Delete(zipPath);
                }
                // 更新时间戳，表示已缓存最新版本
                File.SetLastWriteTime(exePath, currentExeTime);
            }
            
            // 启动应用
            ProcessStartInfo info = new ProcessStartInfo(exePath);
            info.WorkingDirectory = Path.GetDirectoryName(exePath);
            info.UseShellExecute = true;
            Process.Start(info);
            
        } catch (Exception ex) {
            MessageBox.Show("启动失败: " + ex.Message, "启动错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
        }
    }
}
