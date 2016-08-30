package com.andy.tools.warcomparator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;

public class WarComparator {

    public static void main(String[] args) throws IOException {
	if (args.length < 4) {
	    System.err.println("Usage: WarComparator <newWar> <newWarExtractFolder> <oldWar> ");
	    System.exit(1);
	}
	isDifferent(args[0], args[1], args[2], args[3]);
    }

    public static boolean isDifferent(String newFile, String newFileExtractionFolder, String oldFile,
	    String oldFileExtractionFolder) throws IOException {

	Map<String, String> nwifs = extractChecksums(newFile, newFileExtractionFolder);
	Map<String, String> owifs = extractChecksums(oldFile, oldFileExtractionFolder);

	System.out.println("New: " + nwifs.size());
	System.out.println("Old: " + owifs.size());

	if (nwifs.size() != owifs.size()) {
	    return true;
	}

	for (String file : nwifs.keySet()) {

	    if (file.startsWith("META-INF")) {
		System.out.println("Ignoring META-INF files for war: " + file);
		continue;
	    }

	    if (!owifs.containsKey(file)) {
		System.out.println("Old war does not contains new file " + file);
		return true;
	    }

	    if (!file.endsWith(".jar") && !nwifs.get(file).equalsIgnoreCase(owifs.get(file))) {
		System.out.println("Diff file found " + file);
		return true;
	    }
	}

	System.out.println("WARS have same internals files, now comparing jars...");

	for (String file : nwifs.keySet()) {

	    if (file.startsWith("META-INF")) {
		System.out.println("Ignoring META-INF files");
		continue;
	    }

	    // System.out.println(file+": "+ xx.get(file)+" - "+xx2.get(file));
	    if (file.endsWith(".jar")) {
		// 2 jars with different checksum

		if (file.contains("dao"))
		    System.out.println("Comparing jar: " + file + ", New: " + nwifs.get(file) + ", old: " + owifs.get(file));

		if (!nwifs.get(file).equalsIgnoreCase(owifs.get(file))) {

		    Map<String, String> njifs = extractChecksums(newFileExtractionFolder + File.separator + file, null);
		    Map<String, String> ojifs = extractChecksums(oldFileExtractionFolder + File.separator + file, null);

		    if (njifs.size() != ojifs.size()) {
			return true;
		    }

		    for (String njif : njifs.keySet()) {

			if (njif.startsWith("META-INF")) {
			    System.out.println("Ignoring META-INF files for jar: " + njif);
			    continue;
			}

			if (!ojifs.containsKey(njif)) {
			    System.out.println("File " + njif + " not found in old jar");
			    return true;
			}

			if (!njifs.get(njif).equalsIgnoreCase(ojifs.get(njif))) {
			    System.out.println("Diff file found " + njif + " in jar");
			    return true;
			}
		    }
		}
	    }
	}

	return false;
    }

    /**
     * Unzip it
     * 
     * @param zipFile
     *            input zip file
     * @param output
     *            zip file output folder
     * @throws IOException
     */
    public static Map<String, String> extractChecksums(String zipFile, String extractLibsFolder) throws IOException {

	Map<String, String> files = new HashMap<String, String>();

	byte[] buffer = new byte[8192];

	long time1 = System.nanoTime();

	try {

	    ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile));

	    ZipEntry ze = zis.getNextEntry();

	    while (ze != null) {

		if (!ze.isDirectory()) {

		    if (ze.getName().endsWith(".jar")) {
			File newFile = new File(extractLibsFolder + File.separator + ze.getName());
			new File(newFile.getParent()).mkdirs();
			FileOutputStream fos = new FileOutputStream(newFile);

			int len;
			while ((len = zis.read(buffer)) > 0) {
			    fos.write(buffer, 0, len);

			}

			fos.close();
			files.put(ze.getName(), getMd5(newFile));
		    } else {
			files.put(ze.getName(), getMd5(zis));
		    }
		}
		ze = zis.getNextEntry();
	    }

	    zis.closeEntry();
	    zis.close();

	    long time2 = System.nanoTime();
	    System.out.println("zip extraction time for " + zipFile + ": " + (time2 - time1));

	    return files;

	} catch (IOException ex) {
	    throw ex;
	}
    }

    private static String getMd5(InputStream iis) throws IOException {

	// long time1 = System.nanoTime();
	String str = DigestUtils.md5Hex(iis);
	// long time2 = System.nanoTime();
	// System.out.println("apache "+(time2-time1));
	return str;
    }

    private static String getMd5(File file) throws IOException {
	HashCode md5 = Files.hash(file, Hashing.md5());
	String md5Hex = md5.toString();
	return md5Hex;
    }
}
