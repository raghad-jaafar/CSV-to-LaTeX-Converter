	import javax.tools.FileObject;
	import java.io.*;
	import java.util.Arrays;
	import java.util.Scanner;
	import java.util.StringTokenizer;
	/**
	 * ------------------------------------------
	 * Written by: Valeria Antosenkova (40126878)
	 *             Raghad Jaafar (40157929)
	 * -------------------------------------------
	 */
public class csv2latex2 {

	public static void main(String[]args){

		Scanner keyboard = new Scanner(System.in);
		System.out.print("How many files do you want to process? > ");
		int numFiles = keyboard.nextInt();

		// array of files used to store files to be processed
		File [] files = new File[numFiles];

		Scanner read = null;
		PrintWriter write = null;
		for (int i = 0; i < files.length; i++){

			System.out.print("Enter the file name: ");
			String fileName = keyboard.next();
			try {	// open file for reading
				read = new Scanner(new FileInputStream("CSV Files/" + fileName + ".csv"));
			}
			catch(FileNotFoundException e){
				System.out.println("Could not open input file " +fileName +" for reading. " +
						"Please check if file exists! Program will terminate after closing any opened files");

				// delete files if any of the files can't be opened for reading
				deleteFiles(DIRECTORY_NAME);
				System.exit(0);
			}
			// store the file in the array if opening was successful
			files[i] = new File(fileName);

			try { // open the file for writing
				write = new PrintWriter(new FileOutputStream( "Latex Files/" + files[i].getName() + ".tex"));
			}
			catch (FileNotFoundException e){
				System.out.println("Error writing to a file.");
				System.exit(0);
			}
			System.out.println("Latex file for " + files[i].getName() + " was opened for writing");
		}

		// process files
		processFilesForValidation(files);

		// open a created .tex file for reading
		System.out.println("enter file to display");
		String fName=keyboard.next();

		BufferedReader bufferedReader;
		try {
			bufferedReader = new BufferedReader(new FileReader("Latex Files/" + fName + ".tex"));
		}
		catch (FileNotFoundException e){
			System.out.println("Invalid name of the file. File was not opened for reading.");
		}

		keyboard.close();
	}

	/**
	 * Processes an array of files by reading CSV files and writing into LATEX files.
	 * If file is valid, information read from the CSV file is written into the LATEX file.
	 * If an attribute or any data are missing, writing to a file stops and the line with the missing information is written
	 * into a log file depending on the type of info missing.
	 *
	 * @param files an array of files
	 */
	public static void processFilesForValidation(File[] files) {

		// streams for outputting missing data into log files
		PrintStream errStream = null;
		PrintStream errStream2 = null;
		try {
			errStream = new PrintStream (new FileOutputStream ("logFileAttr.txt"));
			errStream2 = new PrintStream (new FileOutputStream ("logFileData.txt"));
		}
		catch (FileNotFoundException e){
		}

		// reading and writing of files
		Scanner read = null;
		PrintWriter write = null;

		for (int i = 0; i < files.length; i++){
			try {
				// connect streams  for reading and writing to a file
				read = new Scanner(new FileInputStream("CSV Files/" + files[i].getName() + ".csv"));
				write = new PrintWriter(new FileOutputStream("Latex Files/" + files[i].getName() + ".tex"));

				String title = read.nextLine(); // read title

				String[] heading;
				String st = read.nextLine(); // read the line with attributes

				System.out.println("Writing to a file..\n");
				write.println("\\begin{table}[!ht]");
				write.println("\\begin{tabular}{|l|l|l|l|l|l|}");
				write.println("\\toprule");

				// work with attributes line
				try {
					heading = readAttrib(st);
					for (int z = 0; z < heading.length;z++){
						write.print(heading[z] + " & ");
					}
				}
				catch (CSVFileInvalidException e){
					// redirect output stream of file with missing attributes to a log file
					System.setErr(errStream);

					System.out.println("File " + files[i].getName() + " is invalid: attribute is missing.");
					System.out.println("File is not converted to LATEX");

					// write info about missing attributes into a log file.
					System.err.println("File" + files[i].getName() + "is invalid: attribute is missing.");
					System.err.println("At line " + st);

					write.close();
				}
				write.println("\\" + "\\midrule");


				// work with the data of the file
				String[] data;
				String dataLine = null;
				int lineNum=2;
				try {
					while(read.hasNext()) {
						dataLine=read.nextLine();
						data = readData(dataLine);
						for(int x=0;x<data.length;x++) {
							write.print(data[x]+" & ");
						}
						if (lineNum == 5){
							write.println("\\" + "\\bottomrule");
						}
						else {
							write.println("\\" + "\\midrule");
						}
						lineNum++;
					}
				}
				catch(CSVDataMissing e) {
					// redirect output stream of file with missing attributes to a log file
					System.setErr(errStream2);

					System.out.println("In file " + files[i].getName() + " not converted to LATEX : missing data");
					System.out.println("At line " + lineNum);

					System.err.println("File " + files[i].getName() + "at line " + lineNum);
					System.err.println(dataLine);

					write.close();
				}
				write.println("\\end{tabular}");
				write.println("\\caption{"+title +"}");
				write.println("\\label{"+files[i].getName()+"}");
				write.println("\\end{table}");
				write.close();
			}
			catch (FileNotFoundException e) {
				System.out.println("Could not open input file " + files[i].getName() + " for reading. ");
			}
		}
	}

	/**
	 * String with the folder name where latex files are written
	 */
	private static final String DIRECTORY_NAME = "Latex Files/";

	/**
	 * Deletes the files and prints the msg whether the file is deleted or not
	 * @param dirName a String of the folder with latex files
	 */
	public static void deleteFiles(String dirName)
	{
		File fileDir = new File(dirName);
		File[] files = fileDir.listFiles();
		for (File file : files)
		{
			boolean isDeleted = file.delete();
			System.out.println(file.getAbsolutePath() + " isDeleted = "
					+ isDeleted);
		}
	}

	/**
	 * Processes the line of attributes from the CSV file
	 * @param attrLine a String of attributes
	 * @return an array of attributes
	 * @throws CSVFileInvalidException is attribute is missing
	 */
	public static String[] readAttrib(String attrLine) throws CSVFileInvalidException {
		String[] str;
		String[] heading;
		str = attrLine.split(",");

		heading = new String[str.length];

		for (int k = 0; k < str.length;k++){
			if (str[k].isEmpty()){
				throw new CSVFileInvalidException();
			}
			else {
				heading[k] = str[k];
			}
		}
		return heading;
	}

	/**
	 * Processes the data from CSV files
	 * @param dataLine a string of data
	 * @return array of data
	 * @throws CSVDataMissing if data is missing
	 */
	public static String[] readData(String dataLine)throws CSVDataMissing{
		String[] str;
		String[] data;
		str=dataLine.split(",");
		data =new String[str.length];
		for(int i=0;i<str.length;i++) {
			if(str[i].isEmpty()) {
				throw new CSVDataMissing();
			}
			else {
				data[i] = str[i];
			}
		}
		return data;
	}

	/**
	 * Exception for invalid files
	 */
	public static class CSVFileInvalidException extends IOException{
		public CSVFileInvalidException (){

		}
		public CSVFileInvalidException(String msg){
			super(msg);
		}
	}

	/**
	 * Exception for missing data
	 */
	public static class CSVDataMissing extends IOException {
		public CSVDataMissing(String msg)
		{
			super(msg);
		}

		public CSVDataMissing() {

		}
	}

}