using System;
using System.Text;
using System.Runtime.InteropServices;
using System.Diagnostics;
using System.Windows.Forms;
using System.Threading;
using System.IO;
using System.Linq;

namespace vrnode
{
    // ref. https://hgotoh.jp/wiki/doku.php/documents/windows/windows-023
    class VrController
    {
        [DllImport("user32.dll")]
        private static extern IntPtr GetWindow(IntPtr hWnd, uint uCmd);

        [DllImport("user32.dll")]
        private static extern IntPtr GetParent(IntPtr hWnd);

        [DllImport("user32.dll")]
        private static extern int PostMessage(IntPtr hWnd, uint Msg, int wParam, int lParam);

        [DllImport("user32.dll")]
        private static extern int SendMessage(IntPtr hWnd, uint Msg, int wParam, int lParam);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern int SendMessage(IntPtr hWnd, uint Msg, int wParam, StringBuilder lParam);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        public static extern IntPtr FindWindow(string lpClassName, string lpWindowName);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern int GetWindowText(IntPtr hWnd, StringBuilder lpString, int nMaxCount);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern int GetWindowTextLength(IntPtr hWnd);

        [DllImport("user32.dll", CharSet = CharSet.Auto, SetLastError = true)]
        private static extern IntPtr FindWindowEx(IntPtr hWndParent, IntPtr hWndChildAfter, string lpszClass, string lpString);

        const uint WM_NULL = 0x0000;
        const uint GW_HWNDNEXT = 0x0002;
        const uint GW_CHILD = 0x0005;
        const uint WM_SETTEXT = 0x000C;
        const uint EM_SETSEL = 0x00B1;
        const uint BM_CLICK = 0x00F5;
        const uint WM_COMMAND = 0x0111;
        const uint WM_PASTE = 0x0302;

        private int saveDialogueBackoff;
        private int saveDialogueAwait;

        public VrController(int saveDialogueAwait, int saveDialogueBackoff)
        {
            this.saveDialogueBackoff = saveDialogueBackoff;
            this.saveDialogueAwait = saveDialogueAwait;
        }

        private string ExpectedWindowTitle()
        {
            return "VOICEROID＋ 東北きりたん EX";
        }

        private string ExpectedDialogueTitle()
        {
            return "音声ファイルの保存";
        }

        private IntPtr GetMainWindowHandle(string title)
        {
            Console.WriteLine("Search " + title + " handler...");
            IntPtr handle = IntPtr.Zero;

            foreach(Process p in Process.GetProcesses())
            {
                if (p.MainWindowHandle != IntPtr.Zero)
                {
                    Console.WriteLine(p.MainWindowTitle);
                    if (p.MainWindowTitle.StartsWith(title))
                    {
                        handle = p.MainWindowHandle;
                        Console.WriteLine("found: " + handle);
                        break;
                    }
                }
            }

            return handle;
        }

        private IntPtr PlayButtonWindowHandle(IntPtr mainWindowHandle)
        {
            IntPtr hWndWorkPtr = mainWindowHandle;

            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第1層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第2層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_HWNDNEXT); // 第2層2番目ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第3層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第4層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第5層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第6層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第7層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_HWNDNEXT); // 第7層2番目ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第8層ウインドウ(再生ボタン)

            Console.WriteLine("PlayButtonWindowHandle found:" + hWndWorkPtr);

            return hWndWorkPtr;
        }

        private void PlayTalk(string text, IntPtr main)
        {
            IntPtr play = PlayButtonWindowHandle(main);
            Console.WriteLine("Automatically press play button.");
            SendMessage(play, BM_CLICK, 0, 0);
        }

        private IntPtr SaveButtonWindowHandle(IntPtr mainWindowHandle)
        {
            IntPtr hWndWorkPtr = PlayButtonWindowHandle(mainWindowHandle);

            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_HWNDNEXT);    // 第8層2番目ウインドウ(停止ボタン)
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_HWNDNEXT);    // 第8層3番目ウインドウ(音声保存ボタン)

            Console.WriteLine("SaveButtonWindowHandle found: " + hWndWorkPtr);

            return hWndWorkPtr;
        }

        private IntPtr SaveDialogueHandle(string title, IntPtr mainWindowHandle, int retry)
        {
            if (retry == 0)
            {
                Console.WriteLine("SaveDialogueHandle timed out.");
                return IntPtr.Zero;
            }

            IntPtr handle = FindWindow("#32770", title);

            while(!handle.Equals(IntPtr.Zero))
            {
                IntPtr parent = GetParent(handle);
                if (parent.Equals(mainWindowHandle))
                {
                    break;
                } else
                {
                    handle = GetWindow(handle, GW_HWNDNEXT);
                }
            }

            if (handle != IntPtr.Zero)
            {
                Console.WriteLine("SaveDialogueHandle found: " + handle);
                return handle;
            }

            Thread.Sleep(saveDialogueBackoff);

            return SaveDialogueHandle(title, mainWindowHandle, retry - 1);
        }

        private IntPtr SaveDialogueTextboxHandle(IntPtr hWndDlgWinPtr)
        {
            IntPtr textbox = hWndDlgWinPtr;

            textbox = GetWindow(textbox, GW_CHILD); // 第1層ウインドウ
            textbox = GetWindow(textbox, GW_CHILD); // 第2層ウインドウ
            textbox = GetWindow(textbox, GW_CHILD); // 第3層ウインドウ
            textbox = GetWindow(textbox, GW_CHILD); // 第4層ウインドウ(コンボボックス)
            textbox = GetWindow(textbox, GW_CHILD); // 第5層ウインドウ(テキストボックス)

            return textbox;
        }

        private void SaveTalk(string text, IntPtr main, string filename)
        {
            IntPtr save = SaveButtonWindowHandle(main);

            Console.WriteLine("Automatically press save button.");
            PostMessage(save, BM_CLICK, 0, 0); // 音声保存ボタンクリック

            IntPtr hWndDlgWinPtr = SaveDialogueHandle(ExpectedDialogueTitle(), main, 10);

            Thread.Sleep(saveDialogueAwait);

            IntPtr textbox = SaveDialogueTextboxHandle(hWndDlgWinPtr);

            Console.WriteLine("Automatically edit " + filename + ": " + textbox);
            SendMessage(textbox, WM_SETTEXT, 0, new StringBuilder(filename));

            IntPtr button = FindWindowEx(hWndDlgWinPtr, IntPtr.Zero, "Button", "保存(&S)"); // 配下の保存ボタン

            Console.WriteLine("Automatically press save button: " + button);
            SendMessage(button, BM_CLICK, 0, 0);
        }

        private IntPtr TextBoxWindowHandle(IntPtr mainWindowHandle)
        {
            IntPtr hWndWorkPtr = mainWindowHandle;

            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第1層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第2層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_HWNDNEXT); // 第2層2番目ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第3層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第4層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第5層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第6層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第7層ウインドウ
            hWndWorkPtr = GetWindow(hWndWorkPtr, GW_CHILD);    // 第8層ウインドウ(テキストボックス) 

            return hWndWorkPtr;
        }

        private void setClipboard(string text)
        {
            Thread t = new Thread(() => {
                Clipboard.SetText(text, TextDataFormat.Text);
            });
            t.SetApartmentState(ApartmentState.STA);
            t.Start();
            t.Join();
        }

        private void WriteWav(string text, string filename)
        {
            IntPtr main = GetMainWindowHandle(ExpectedWindowTitle());
            IntPtr textbox = TextBoxWindowHandle(main);

            Console.WriteLine("Automatically paste text: " + text);
            SendMessage(textbox, EM_SETSEL, 0, -1); // 全テキストを選択
            setClipboard(text);
            SendMessage(textbox, WM_PASTE, 0, 0); // クリップボードの内容をペースト

            //PlayTalk(text, main);
            SaveTalk(text, main, filename);
        }

        private byte[] ReadWav(string directory, string filename, int retry)
        {
            if (retry == 0)
            {
                throw new InvalidOperationException("ReadWav timed out.");
            }

            foreach (var f in new DirectoryInfo(directory).GetFiles().Where(f => Path.GetFileNameWithoutExtension(f.Name) == filename))
            {
                Console.WriteLine(Path.GetExtension(f.Name) + " found.");
                if (Path.GetExtension(f.Name) == ".wav")
                {
                    return File.ReadAllBytes(f.FullName);
                }
            }

            Thread.Sleep(50);

            return ReadWav(directory, filename, retry - 1);
        }

        public byte[] Talk(string text, string directory, string filename)
        {
            foreach (var f in new DirectoryInfo(directory).GetFiles().Where(f => Path.GetFileNameWithoutExtension(f.Name) == filename))
            {
                Console.WriteLine("Delete duplicated file: " + f);
                f.Delete();
            }

            WriteWav(text, filename);

            return ReadWav(directory, filename, 10);
        }
    }
}
