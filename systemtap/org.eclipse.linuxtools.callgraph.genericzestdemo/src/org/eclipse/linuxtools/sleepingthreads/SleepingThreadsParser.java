package org.eclipse.linuxtools.sleepingthreads;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.callgraph.core.Helper;
import org.eclipse.linuxtools.internal.callgraph.core.SystemTapParser;

public class SleepingThreadsParser extends SystemTapParser {
	protected String contents;
	protected Deque<Integer> current;
	protected int serial;

	@Override
	public IStatus nonRealTimeParsing() {
		contents = Helper.readFile(sourcePath);
		System.out.println(contents);
		return Status.OK_STATUS;
	}

	@Override
	protected void initialize() {
		current = new ArrayDeque<Integer>();
		serial = 0;
		data = new ArrayList<XMLData>();
	}

	@SuppressWarnings("unchecked")
	@Override
	public IStatus realTimeParsing() {
		if (!(internalData instanceof BufferedReader))
			return Status.CANCEL_STATUS;

		if (data == null)
			data = new ArrayList<XMLData>();

		BufferedReader buff = (BufferedReader) internalData;

		String line;
		try {
			while ((line = buff.readLine()) != null) {
				if (monitor.isCanceled())
					return Status.CANCEL_STATUS;
				
				if (line.charAt(0) == '+') {
					//Call
					XMLData item = new XMLData();
					item.setIsNode(true);
					item.setName(line.substring(1));
					if (current.size() < 1) {
						//First node
						item.setParent(-1);
					}
					else 
						item.setParent(current.peekFirst());
					((ArrayList<XMLData>) data).add(item);
					current.offerFirst(serial);
					serial++;
					
				}
				
				else if (line.charAt(0) == '-') {
					//Return
					
					current.pollFirst();
				}
				
				else {
					XMLData item = new XMLData();
					item.setIsNode(false);
					item.setParent(current.peekFirst());
					item.setText(line);
					((ArrayList<XMLData>) data).add(item);
				}
			}
			view.update();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

}
