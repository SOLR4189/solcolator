package solcolator.solcolator.common;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

public class FileUtils {
	public static File getLatestModifiedFileInDir(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter() {
			
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for (File file : files) {
			if (file.lastModified() > lastMod) {
				choice = file;
				lastMod = file.lastModified();
			}
		}
		
		return choice;
	}
	
	public static boolean dirExist(String dirPath) {
		File dir = new File(dirPath);
		
		return dir.exists();
	}
	
	public static boolean dirEmpty(String dirPath) {
		File dir = new File(dirPath);

		return dir.list().length == 0;
	}
	
	public static boolean fileExist(String filePath) {
		File file = new File(filePath);
		
		return file.exists();
	}
	
	/**
	 * Delete all files are older than X days
	 * @param days - how many days to save files
	 * @param dirPath - directory that contains files for removing
	 */
	public static void deleteOlderThanXDays(int days, String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles();

		for (File file : files) {
			long diff = new Date().getTime() - file.lastModified();
			if (diff > days * 24 * 60 * 60 * 1000) {
				file.delete();
			}
		}
	}
}
