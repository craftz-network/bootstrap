package net.xxathyx.craftz.bootstrap.updater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.time.LocalDateTime;

import javafx.application.Platform;

import net.xxathyx.craftz.bootstrap.Main;

public class Updater {
	
	public enum OS {
	    WINDOWS, LINUX, MAC, SOLARIS, OTHER;
	};
	
	private static String fs = File.separator;
	
	public Main main;
	
	public Thread currentThread;
	
	public long onlineLength = 0;
	public long downloaded = 0;
	
	public Updater(Main main) throws IOException {
        URL url = new URL("https://craftz.net/download/launcher.jar");
        URLConnection urlConnection = url.openConnection();
        onlineLength = urlConnection.getContentLengthLong();
        this.main = main;
	}
	
	public Updater() {}
	
	public static File getGameFolder() {
		return new File((getOS().equals(OS.WINDOWS) ? System.getenv("APPDATA") : System.getProperty("user.home")) + fs + ".craftz" + fs);
	}
	
	public File getPidFile() {
		return new File(getGameFolder() + fs + "updater" + fs, "pid.txt");
	}
	
	public File getLibrariesFolder() {
		return new File(getGameFolder() + fs + "updater" + fs + "lib" + fs);
	}
	
	public static File getLogsFolder() {
		return new File(getGameFolder() + fs + "updater" + fs + "logs" + fs);
	}
	
	public File getLauncherFile() {
		return new File(getGameFolder() + fs + "launcher.jar");
	}
	
	public boolean isUpdated() throws IOException {
		
		File launcherFile = getLauncherFile();
		if(!launcherFile.exists()) return false;
        
		if(onlineLength == launcherFile.length()) return true;
        
		return false;
	}
	
	public void download() throws InterruptedException {
	    
	    String archive = "https://craftz.net/download/launcher.jar";  
	    File jar = getLauncherFile();
	    
	    Thread thread = new Thread(() -> {
	    	
	        try {
			    URL url = new URL(archive);  
			    URLConnection connection = url.openConnection();  
			    InputStream inputStream = connection.getInputStream();  
			    FileOutputStream outputStream = new FileOutputStream(jar);
			    
			    final byte[] bt = new byte[1024];
			    int len;
			    while((len = inputStream.read(bt)) != -1) {
			    	outputStream.write(bt, 0, len);
			    	downloaded = downloaded + len;
			    	float progress = (float)downloaded/(float)onlineLength;
			    	Platform.runLater(() -> {
				    	main.progressBar.setProgress(progress);
		            });
			    }
			    outputStream.close();
			    launch();
			}catch (IOException e) {
				e.printStackTrace();
			}
	    });
	    currentThread = thread;
	    thread.start();
	}
	
	public String getLibPath(File file) {
		String separator = "";
		if(System.getProperty("os.name").contains("Windows")) separator = "\"";
		return separator + file.getPath() + separator;
	}
	
	@SuppressWarnings("deprecation")
	public void launch() throws IOException {
		
		if(Double.parseDouble(System.getProperty("java.class.version")) <= 52) {
			new ProcessBuilder(
					"java",
					"-jar",
					getLauncherFile().getPath()
			).start();
		}else {
			Runtime.getRuntime().exec("java " + "--module-path " + getLibPath(getLibrariesFolder()) + " --add-modules=" + "javafx.controls,javafx.fxml,javafx.media,javafx.web"+ " -jar "+ getLauncherFile().getPath());
		}
		System.exit(0);
	}
	
	public static String time() {
		LocalDateTime date  = LocalDateTime.now();
		return "["+date.getDayOfMonth()+"/"+date.getMonthValue()+"/"+date.getYear()+" "+date.getHour()+":"+date.getMinute()+":"+date.getSecond()+"] ";
	}
	
    public static OS getOS() {
    	
        String operatingSystem = java.lang.System.getProperty("os.name").toLowerCase();
        
        if(operatingSystem.contains("win")) { return OS.WINDOWS;
        }else if (operatingSystem.contains("nix") || operatingSystem.contains("nux") || operatingSystem.contains("aix")) {return OS.LINUX;
        }else if (operatingSystem.contains("mac")) {return OS.MAC;
        }else if (operatingSystem.contains("sunos")) {return OS.SOLARIS;}
        return OS.OTHER;
    }
}