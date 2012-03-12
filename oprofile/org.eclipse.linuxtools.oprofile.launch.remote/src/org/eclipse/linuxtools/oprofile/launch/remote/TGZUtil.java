package org.eclipse.linuxtools.oprofile.launch.remote;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

public class TGZUtil {

	/**
	 * 
	 * @param tgzFile tgz file to extract
	 * @param targetDir dir to which extract archive, must exist
	 * @param monitor
	 * @throws IOException 
	 */
	public static void extractTgz(String tgzFile, String targetDir) throws IOException {
/*		
		// Ungzip file
		InputStream is = new GZIPInputStream(new FileInputStream(tgzFile));
		BufferedInputStream ibs = new BufferedInputStream(is);
		FileOutputStream ofs= new FileOutputStream(targetDir + File.separator + "tmp.tar");
		BufferedOutputStream obs = new BufferedOutputStream(ofs);
		IOUtils.copy(ibs, obs);
		ibs.close();
		obs.close();

		
		// Untar file
		//is = new FileInputStream(targetDir + File.separator + ".tar");
*/
		InputStream is = new GZIPInputStream(new FileInputStream(tgzFile));
		TarArchiveInputStream tis = new TarArchiveInputStream(is);
		TarArchiveEntry te = tis.getNextTarEntry();
		while ( te != null ) {
			File destPath = new File(targetDir + File.separatorChar + te.getName());
			if ( ! te.isDirectory() ) {
				FileOutputStream fout = new FileOutputStream(destPath);
				BufferedOutputStream bos = new BufferedOutputStream(fout);
				long size = te.getSize();
				for (long n=0; n<size; n++) {
					int b = tis.read();
					bos.write(b);
				}
				bos.close();
			} else {
				if ( !destPath.mkdirs() ) {
					throw new IOException("Can't create dir " + destPath);
				}
			}
		         te = tis.getNextTarEntry();
		}
		
		tis.close();

	}

	
	public static void main(String[] args) throws IOException {
		extractTgz(args[0], System.getProperty("user.dir"));
	}
}
