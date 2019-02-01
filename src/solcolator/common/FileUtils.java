package solcolator.common;

import java.io.File;
import java.io.FileFilter;
import java.util.Date;

/**
 * Helper class for working with file and directories
 * @author Nickolay
 *
 */
public class FileUtils {
	/**
	 * Returns the latest modified file in the directory
	 * If directory is empty, NULL will be returned.
	 * @param dir - In which will be selected the latest modified file
	 * @return - The latest modified file or NULL
	 */
	public static File getLatestModifiedFileInDir(String dir) {
		File fl = new File(dir);
		File[] files = fl.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file) {
				return file.isFile();
			}
		});
		
		long lastMod = Long.MIN_VALUE;
		File choice = null;
		for(File file : files) {
			if (file.lastModified() > lastMod) {
				choice = file;
				lastMod = file.lastModified();
			}
		}
		
		return choice;
	}
	
	/**
	 * Checks if directory from input path parameter exists
	 * @param dirPath - Path to checking directory
	 * @return true(exists) or false(doesn't exist)
	 */
	public static boolean dirExists(String dirPath) {
		File dir = new File(dirPath);
		
		return dir.exists();
	}
	
	/**
	 * Checks if directory from input path parameter empty
	 * @param dirPath - Path to checking directory
	 * @return true(empty) or false(isn't empty)
	 */
	public static boolean dirEmpty(String dirPath) {
		File dir = new File(dirPath);
		
		return dir.list().length == 0;
	}
	
	/**
	 * Checks if file from input path parameter exists
	 * @param filePath - Path to checking file
	 * @return true(exists) or false(doesn't exist)
	 */
	public static boolean fileExists(String filePath) {
		File file = new File(filePath);
		
		return file.exists();
	}
	
	/**
	* Delete all files are older than X days
	* @param days - how many days to save files
	* @dirPath - directory that contains files for removing
	*/
	public static void deleteOlderThanXDays(int days, String dirPath) {
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		
		for(File file : files) {
			long diff = new Date().getTime() - file.lastModified();
			if (diff > days * 24 * 60 * 60 * 1000) {
				file.delete();
			}
		}
	}
}
