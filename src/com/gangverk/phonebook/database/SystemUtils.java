package com.gangverk.phonebook.database;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class SystemUtils {
	public boolean has_iset_info = false;
	private ArrayList<InstructionSet> supportedInstructionSets;

	public enum InstructionSet {
		neon, vfp, vfpv3
	}

	public SystemUtils() {
		supportedInstructionSets = new ArrayList<InstructionSet>();
        String commandLine = "cat /proc/cpuinfo";
        Process process;
		try {
			process = Runtime.getRuntime().exec(commandLine);
	        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	        String curLine;
	        while((curLine = bufferedReader.readLine()) != null) {
	        	if(curLine.contains("Features")) {
	        		String[] isets = curLine.split(" ");
	        		for(String iset : isets) {
	        			try {
	        				supportedInstructionSets.add(InstructionSet.valueOf(iset));
	        			} catch (IllegalArgumentException iae) {
	        			}
	        		}
		        	has_iset_info = true;
		        	break;
	        	}
	        }
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Indicates whether given instruction set is supported by the current architecture
	 * @param iset The instruction set
	 * @return true if instruction set is supported, false otherwise.
	 */
	public boolean supportsInstructionSet(InstructionSet iset) {
		return supportedInstructionSets.contains(iset);
	}
	
	
	static public void copy(String src, String dst) throws IOException {
		copy(new File(src), new File(dst));
	}
	
	/**
	 * Copies from one file to another, creating destination file if required.
	 * @param src File to copy data from.
	 * @param dst File to copy data to.
	 * @throws IOException
	 */
	static public void copy(File src, File dst) throws IOException {
	    FileChannel inChannel = new FileInputStream(src).getChannel();
	    FileChannel outChannel = new FileOutputStream(dst).getChannel();
	    
	    try {
	    	inChannel.transferTo(0, inChannel.size(), outChannel);
	    } finally {
	    	if(inChannel != null) {
	    		inChannel.close();
	    	}
	    	if(outChannel != null) {
	    		outChannel.close();
	    	}
	    }
	}
	
	/**
	 * Reads a stream and writes it into a string. Closes inputStream when done.
	 * @param inputStream The stream to read
	 * @return A string, containing stream data
	 * @throws java.io.IOException
	 */
	public static String stringFromStream(InputStream inputStream) throws java.io.IOException{
		String encoding = "UTF-8";
		StringBuilder builder = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, encoding));
		String line;
		while((line = reader.readLine()) != null) {
			builder.append(line);
		}
		reader.close();
		return builder.toString();
	}

	/**
	 * Copies bytes between streams. Closes both streams when done.
	 * @param inStream The stream to copy from
	 * @param outStream The stream to copy to
	 * @return The amount of bytes copied
	 * @throws IOException
	 */
	public static int copyInputStreamToOutputStream(InputStream inStream, OutputStream outStream) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(inStream);
		BufferedOutputStream bos = new BufferedOutputStream(outStream);
		int totalLen = 0;
		
		int bufSize = 1024*4;
    	byte[] buffer = new byte[bufSize];
    	int len;
    	while((len = bis.read(buffer,0,bufSize)) > -1) {
    		bos.write(buffer,0,len);
    		totalLen += len;
    	}
    	bis.close();
    	bos.close();
    	return totalLen;
	}
	
	/**
	 * Merges two String arrays
	 * @param first array previous array
	 * @param other array second array
	 * @return concatenated array
	 */
	public static String[] mergeStringArrays(String[] first, String[] second) {
		String[] result = new String[first.length + second.length];
		System.arraycopy(first, 0, result, 0, first.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

}