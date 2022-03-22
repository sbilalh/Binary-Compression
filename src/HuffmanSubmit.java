import java.util.*;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;


public class HuffmanSubmit implements Huffman {

	/** 
	 * FIELDS
	 *********/

	/** Hash Map to be populated by a recursive function (code) and utilized by encode method for creating output */
	public HashMap<Character, String> codeMap = new HashMap<Character, String>();


	/** 
	 * NESTED CLASSES 
	 ****************/

	/** BinaryIn class */
	public final class BinaryIn {
		private static final int EOF = -1;   // end of file

		private BufferedInputStream in;      // the input stream
		private int buffer;                  // one character buffer
		private int n;                       // number of bits left in buffer

		/**
		 * Initializes a binary input stream from standard input.
		 */
		public BinaryIn() {
			in = new BufferedInputStream(System.in);
			fillBuffer();
		}

		/**
		 * Initializes a binary input stream from an {@code InputStream}.
		 *
		 * @param is the {@code InputStream} object
		 */
		public BinaryIn(InputStream is) {
			in = new BufferedInputStream(is);
			fillBuffer();
		}

		/**
		 * Initializes a binary input stream from a socket.
		 *
		 * @param socket the socket
		 */
		public BinaryIn(Socket socket) {
			try {
				InputStream is = socket.getInputStream();
				in = new BufferedInputStream(is);
				fillBuffer();
			}
			catch (IOException ioe) {
				System.err.println("Could not open " + socket);
			}
		}

		/**
		 * Initializes a binary input stream from a URL.
		 *
		 * @param url the URL
		 */
		public BinaryIn(URL url) {
			try {
				URLConnection site = url.openConnection();
				InputStream is     = site.getInputStream();
				in = new BufferedInputStream(is);
				fillBuffer();
			}
			catch (IOException ioe) {
				System.err.println("Could not open " + url);
			}
		}

		/**
		 * Initializes a binary input stream from a filename or URL name.
		 *
		 * @param name the name of the file or URL
		 */
		public BinaryIn(String name) {

			try {
				// first try to read file from local file system
				File file = new File(name);
				if (file.exists()) {
					FileInputStream fis = new FileInputStream(file);
					in = new BufferedInputStream(fis);
					fillBuffer();
					return;
				}

				// next try for files included in jar
				URL url = getClass().getResource(name);

				// or URL from web
				if (url == null) {
					url = new URL(name);
				}

				URLConnection site = url.openConnection();
				InputStream is     = site.getInputStream();
				in = new BufferedInputStream(is);
				fillBuffer();
			}
			catch (IOException ioe) {
				System.err.println("Could not open " + name);
			}
		}

		private void fillBuffer() {
			try {
				buffer = in.read();
				n = 8;
			}
			catch (IOException e) {
				System.err.println("EOF");
				buffer = EOF;
				n = -1;
			}
		}

		/**
		 * Returns true if this binary input stream exists.
		 *
		 * @return {@code true} if this binary input stream exists;
		 *         {@code false} otherwise
		 */
		public boolean exists()  {
			return in != null;
		}

		/**
		 * Returns true if this binary input stream is empty.
		 *
		 * @return {@code true} if this binary input stream is empty;
		 *         {@code false} otherwise
		 */
		public boolean isEmpty() {
			return buffer == EOF;
		}

		/**
		 * Reads the next bit of data from this binary input stream and return as a boolean.
		 *
		 * @return the next bit of data from this binary input stream as a {@code boolean}
		 * @throws NoSuchElementException if this binary input stream is empty
		 */
		public boolean readBoolean() {
			if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
			n--;
			boolean bit = ((buffer >> n) & 1) == 1;
			if (n == 0) fillBuffer();
			return bit;
		}

		/**
		 * Reads the next 8 bits from this binary input stream and return as an 8-bit char.
		 *
		 * @return the next 8 bits of data from this binary input stream as a {@code char}
		 * @throws NoSuchElementException if there are fewer than 8 bits available
		 */
		public char readChar() {
			if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

			// special case when aligned byte
			if (n == 8) {
				int x = buffer;
				fillBuffer();
				return (char) (x & 0xff);
			}

			// combine last N bits of current buffer with first 8-N bits of new buffer
			int x = buffer;
			x <<= (8 - n);
			int oldN = n;
			fillBuffer();
			if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
			n = oldN;
			x |= (buffer >>> n);
			return (char) (x & 0xff);
			// the above code doesn't quite work for the last character if N = 8
			// because buffer will be -1
		}


		/**
		 * Reads the next r bits from this binary input stream and return as an r-bit character.
		 *
		 * @param  r number of bits to read
		 * @return the next {@code r} bits of data from this binary input streamt as a {@code char}
		 * @throws NoSuchElementException if there are fewer than {@code r} bits available
		 * @throws IllegalArgumentException unless {@code 1 <= r <= 16}
		 */
		public char readChar(int r) {
			if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value of r = " + r);

			// optimize r = 8 case
			if (r == 8) return readChar();

			char x = 0;
			for (int i = 0; i < r; i++) {
				x <<= 1;
				boolean bit = readBoolean();
				if (bit) x |= 1;
			}
			return x;
		}


		/**
		 * Reads the remaining bytes of data from this binary input stream and return as a string. 
		 *
		 * @return the remaining bytes of data from this binary input stream as a {@code String}
		 * @throws NoSuchElementException if this binary input stream is empty or if the number of bits
		 *         available is not a multiple of 8 (byte-aligned)
		 */
		public String readString() {
			if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

			StringBuilder sb = new StringBuilder();
			while (!isEmpty()) {
				char c = readChar();
				sb.append(c);
			}
			return sb.toString();
		}


		/**
		 * Reads the next 16 bits from this binary input stream and return as a 16-bit short.
		 *
		 * @return the next 16 bits of data from this binary input stream as a {@code short}
		 * @throws NoSuchElementException if there are fewer than 16 bits available
		 */
		public short readShort() {
			short x = 0;
			for (int i = 0; i < 2; i++) {
				char c = readChar();
				x <<= 8;
				x |= c;
			}
			return x;
		}

		/**
		 * Reads the next 32 bits from this binary input stream and return as a 32-bit int.
		 *
		 * @return the next 32 bits of data from this binary input stream as a {@code int}
		 * @throws NoSuchElementException if there are fewer than 32 bits available
		 */
		public int readInt() {
			int x = 0;
			for (int i = 0; i < 4; i++) {
				char c = readChar();
				x <<= 8;
				x |= c;
			}
			return x;
		}

		/**
		 * Reads the next r bits from this binary input stream return as an r-bit int.
		 *
		 * @param  r number of bits to read
		 * @return the next {@code r} bits of data from this binary input stream as a {@code int}
		 * @throws NoSuchElementException if there are fewer than r bits available
		 * @throws IllegalArgumentException unless {@code 1 <= r <= 32}
		 */
		public int readInt(int r) {
			if (r < 1 || r > 32) throw new IllegalArgumentException("Illegal value of r = " + r);

			// optimize r = 32 case
			if (r == 32) return readInt();

			int x = 0;
			for (int i = 0; i < r; i++) {
				x <<= 1;
				boolean bit = readBoolean();
				if (bit) x |= 1;
			}
			return x;
		}

		/**
		 * Reads the next 64 bits from this binary input stream and return as a 64-bit long.
		 *
		 * @return the next 64 bits of data from this binary input stream as a {@code long}
		 * @throws NoSuchElementException if there are fewer than 64 bits available
		 */
		public long readLong() {
			long x = 0;
			for (int i = 0; i < 8; i++) {
				char c = readChar();
				x <<= 8;
				x |= c;
			}
			return x;
		}

		/**
		 * Reads the next 64 bits from this binary input stream and return as a 64-bit double.
		 *
		 * @return the next 64 bits of data from this binary input stream as a {@code double}
		 * @throws NoSuchElementException if there are fewer than 64 bits available
		 */
		public double readDouble() {
			return Double.longBitsToDouble(readLong());
		}

		/**
		 * Reads the next 32 bits from this binary input stream and return as a 32-bit float.
		 *
		 * @return the next 32 bits of data from this binary input stream as a {@code float}
		 * @throws NoSuchElementException if there are fewer than 32 bits available
		 */
		public float readFloat() {
			return Float.intBitsToFloat(readInt());
		}


		/**
		 * Reads the next 8 bits from this binary input stream and return as an 8-bit byte.
		 *
		 * @return the next 8 bits of data from this binary input stream as a {@code byte}
		 * @throws NoSuchElementException if there are fewer than 8 bits available
		 */
		public byte readByte() {
			char c = readChar();
			return (byte) (c & 0xff);
		}
	}

	/** BinaryOut class */
	public final class BinaryOut {

		private BufferedOutputStream out;  // the output stream
		private int buffer;                // 8-bit buffer of bits to write out
		private int n;                     // number of bits remaining in buffer


		/**
		 * Initializes a binary output stream from standard output.
		 */
		public BinaryOut() {
			out = new BufferedOutputStream(System.out);
		}

		/**
		 * Initializes a binary output stream from an {@code OutputStream}.
		 * @param os the {@code OutputStream}
		 */
		public BinaryOut(OutputStream os) {
			out = new BufferedOutputStream(os);
		}

		/**
		 * Initializes a binary output stream from a file.
		 * @param filename the name of the file
		 */
		public BinaryOut(String filename) {
			try {
				OutputStream os = new FileOutputStream(filename);
				out = new BufferedOutputStream(os);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Initializes a binary output stream from a socket.
		 * @param socket the socket
		 */
		public BinaryOut(Socket socket) {
			try {
				OutputStream os = socket.getOutputStream();
				out = new BufferedOutputStream(os);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Writes the specified bit to the binary output stream.
		 * @param x the bit
		 */
		private void writeBit(boolean x) {
			// add bit to buffer
			buffer <<= 1;
			if (x) buffer |= 1;

			// if buffer is full (8 bits), write out as a single byte
			n++;
			if (n == 8) clearBuffer();
		} 

		/**
		 * Writes the 8-bit byte to the binary output stream.
		 * @param x the byte
		 */
		private void writeByte(int x) {
			assert x >= 0 && x < 256;

			// optimized if byte-aligned
			if (n == 0) {
				try {
					out.write(x);
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				return;
			}

			// otherwise write one bit at a time
			for (int i = 0; i < 8; i++) {
				boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
				writeBit(bit);
			}
		}

		// write out any remaining bits in buffer to the binary output stream, padding with 0s
		private void clearBuffer() {
			if (n == 0) return;
			if (n > 0) buffer <<= (8 - n);
			try {
				out.write(buffer);
			}
			catch (IOException e) {
				e.printStackTrace();
			}
			n = 0;
			buffer = 0;
		}

		/**
		 * Flushes the binary output stream, padding 0s if number of bits written so far
		 * is not a multiple of 8.
		 */
		public void flush() {
			clearBuffer();
			try {
				out.flush();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		/**
		 * Closes and flushes the binary output stream.
		 * Once it is closed, bits can no longer be written.
		 */
		public void close() {
			flush();
			try {
				out.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}


		/**
		 * Writes the specified bit to the binary output stream.
		 * @param x the {@code boolean} to write
		 */
		public void write(boolean x) {
			writeBit(x);
		} 

		/**
		 * Writes the 8-bit byte to the binary output stream.
		 * @param x the {@code byte} to write.
		 */
		public void write(byte x) {
			writeByte(x & 0xff);
		}

		/**
		 * Writes the 32-bit int to the binary output stream.
		 * @param x the {@code int} to write
		 */
		public void write(int x) {
			writeByte((x >>> 24) & 0xff);
			writeByte((x >>> 16) & 0xff);
			writeByte((x >>>  8) & 0xff);
			writeByte((x >>>  0) & 0xff);
		}

		/**
		 * Writes the r-bit int to the binary output stream.
		 *
		 * @param  x the {@code int} to write
		 * @param  r the number of relevant bits in the char
		 * @throws IllegalArgumentException unless {@code r} is between 1 and 32
		 * @throws IllegalArgumentException unless {@code x} is between 0 and 2<sup>r</sup> - 1
		 */
		public void write(int x, int r) {
			if (r == 32) {
				write(x);
				return;
			}
			if (r < 1 || r > 32) throw new IllegalArgumentException("Illegal value for r = " + r);
			if (x >= (1 << r))   throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
			for (int i = 0; i < r; i++) {
				boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
				writeBit(bit);
			}
		}


		/**
		 * Writes the 64-bit double to the binary output stream.
		 * @param x the {@code double} to write
		 */
		public void write(double x) {
			write(Double.doubleToRawLongBits(x));
		}

		/**
		 * Writes the 64-bit long to the binary output stream.
		 * @param x the {@code long} to write
		 */
		public void write(long x) {
			writeByte((int) ((x >>> 56) & 0xff));
			writeByte((int) ((x >>> 48) & 0xff));
			writeByte((int) ((x >>> 40) & 0xff));
			writeByte((int) ((x >>> 32) & 0xff));
			writeByte((int) ((x >>> 24) & 0xff));
			writeByte((int) ((x >>> 16) & 0xff));
			writeByte((int) ((x >>>  8) & 0xff));
			writeByte((int) ((x >>>  0) & 0xff));
		}

		/**
		 * Writes the 32-bit float to the binary output stream.
		 * @param x the {@code float} to write
		 */
		public void write(float x) {
			write(Float.floatToRawIntBits(x));
		}

		/**
		 * Write the 16-bit int to the binary output stream.
		 * @param x the {@code short} to write.
		 */
		public void write(short x) {
			writeByte((x >>>  8) & 0xff);
			writeByte((x >>>  0) & 0xff);
		}

		/**
		 * Writes the 8-bit char to the binary output stream.
		 *
		 * @param  x the {@code char} to write
		 * @throws IllegalArgumentException unless {@code x} is betwen 0 and 255
		 */
		public void write(char x) {
			if (x < 0 || x >= 256) throw new IllegalArgumentException("Illegal 8-bit char = " + x);
			writeByte(x);
		}

		/**
		 * Writes the r-bit char to the binary output stream.
		 *
		 * @param  x the {@code char} to write
		 * @param  r the number of relevant bits in the char
		 * @throws IllegalArgumentException unless {@code r} is between 1 and 16
		 * @throws IllegalArgumentException unless {@code x} is between 0 and 2<sup>r</sup> - 1
		 */
		public void write(char x, int r) {
			if (r == 8) {
				write(x);
				return;
			}
			if (r < 1 || r > 16) throw new IllegalArgumentException("Illegal value for r = " + r);
			if (x >= (1 << r))   throw new IllegalArgumentException("Illegal " + r + "-bit char = " + x);
			for (int i = 0; i < r; i++) {
				boolean bit = ((x >>> (r - i - 1)) & 1) == 1;
				writeBit(bit);
			}
		}

		/**
		 * Writes the string of 8-bit characters to the binary output stream.
		 *
		 * @param  s the {@code String} to write
		 * @throws IllegalArgumentException if any character in the string is not
		 *         between 0 and 255
		 */
		public void write(String s) {
			for (int i = 0; i < s.length(); i++)
				write(s.charAt(i));
		}


		/**
		 * Writes the String of r-bit characters to the binary output stream.
		 * @param  s the {@code String} to write
		 * @param  r the number of relevants bits in each character
		 * @throws IllegalArgumentException unless r is between 1 and 16
		 * @throws IllegalArgumentException if any character in the string is not
		 *         between 0 and 2<sup>r</sup> - 1
		 */
		public void write(String s, int r) {
			for (int i = 0; i < s.length(); i++)
				write(s.charAt(i), r);
		}

	}

	/** Node structure for Huffman Tree */
	public class huffmanNode {
	
		// Declaring fields
		Integer freq;
		Character c;		
		huffmanNode left;	
		huffmanNode right;	

		// Constructor
		public huffmanNode() {
		}

	}

	/** Custom comparator to use priority queue as min-heap */
	public class MyComparator implements Comparator<huffmanNode> {

		public int compare(huffmanNode n1, huffmanNode n2)
		{
	  
			return n1.freq - n2.freq;
		}

	}


	/** 
	 * METHODS 
	 *********/

	/** EXTRA CREDIT: 
	 * - Recursive functions to print the Huffman Tree 
	 * - right branches extend horizontally 
	 * - left branches extend vertically
	 * - '*' represents a non-leaf node with a value that is the sum of its two children
	 * */
	public int printNodeRight(huffmanNode root, int indent) { 

		if (root.left != null && root.right != null) {
			System.out.print("*-");
			printNodeRight(root.right,indent+1);
			printNodeLeft(root.left,indent);
		} 
		else {
			System.out.printf("%c(%d)\n", root.c, root.freq);
			for (int i = 0; i < indent; i++) {
				System.out.print("| ");
			}
			System.out.print("\n");
		}

		return 0;

	}
	public int printNodeLeft(huffmanNode root, int indent) {  

		for (int i = 0; i < indent-1; i++) {
			System.out.print("| ");
		}
		printNodeRight(root,indent);

		return 0;

	}

	/** Recursive function to find the codes for each character */
	public void code(huffmanNode root, String str) {

		if (root.left == null && root.right == null) {
			codeMap.put(root.c, str);
			return;
		}
		code(root.left, str + "0");
		code(root.right, str + "1");
		
	}

	/** Encoding */
	public void encode(String inputFile, String outputFile, String freqFile) {

		// Creating input stream

		BinaryIn  in  = new BinaryIn(inputFile);
		String str = new String();

        while (!in.isEmpty()) {
            str += in.readChar();
        }


		// Storing frequency of each occuring character in a hash map

		HashMap<Character, Integer> freqMap = new HashMap<Character, Integer>();
         
        for (int i = 0; i < str.length(); i++) {
            if (freqMap.containsKey(str.charAt(i))) {
                freqMap.put(str.charAt(i), freqMap.get(str.charAt(i)) + 1);
            } else {
                freqMap.put(str.charAt(i), 1);
            }
        }

		 
		// Creating arrays for frequency of each character, ordered by index

		Object[] fArray = freqMap.values().toArray();
		Integer[] freqArray = Arrays.copyOf(fArray, fArray.length, Integer[].class);
		Object[] cArray = freqMap.keySet().toArray();
		int toSize = cArray.length;
		Character[] charArray = new Character[toSize];
		for (int i = 0; i < toSize; i++) {
    		charArray[i] = cArray[i].toString().charAt(0);
		} 

		
		// Creating a text file to store the frequency of each character present in the input file

		String freqText = "";

		for (int i = 0; i < charArray.length; i++) {
			String bin = Integer.toBinaryString(charArray[i]);
			String formatted = String.format("%8s", bin).replaceAll(" ", "0");
			freqText += formatted + ":" + freqArray[i] + "\n";
			
		}
		
		FileWriter writer;
		try {
			writer = new FileWriter(freqFile, false);
			writer.write(freqText);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		

		// Creating a min-heap where each element is of type huffmanNode, containing a character and its corresponding frequency

		PriorityQueue<huffmanNode> freqHeap = new PriorityQueue<huffmanNode>(freqMap.size(), new MyComparator());
		
		for (int i = 0; i < freqMap.size(); i++) {

			huffmanNode n = new huffmanNode();

			n.c = (Character) charArray[i];
			n.freq = (Integer) freqArray[i];
			n.left = null;
			n.right = null;

			freqHeap.add(n);

		}


		// Performing huffman encoding on the min-heap

		huffmanNode root = null;

		while (freqHeap.size() > 1) {
			
			huffmanNode min1 = freqHeap.peek();
			freqHeap.poll();
			huffmanNode min2 = freqHeap.peek();
			freqHeap.poll();
			huffmanNode sum = new huffmanNode();
			sum.freq = min1.freq + min2.freq;
			sum.left = min1;
			sum.right = min2;
			root = sum;
			freqHeap.add(root);

		}

		// EXTRA CREDIT: Printing huffman tree

		System.out.println("Huffman Tree created during encoding: \n");
		printNodeRight(root,0);


		// Creating an output file with bit patterns written as boolean values

		BinaryOut out = new BinaryOut(outputFile);
		root = freqHeap.peek();
		code(root, "");

		for (int i = 0; i < str.length(); i++) {
			for (int j = 0; j < codeMap.get(str.charAt(i)).length(); j++) {
				if (codeMap.get(str.charAt(i)).charAt(j) == '0') {
					out.write(false);
				}
				else {
					out.write(true);
				}
		}
		}
		out.flush();

	}

	/** Decoding */
	public void decode(String inputFile, String outputFile, String freqFile) {

		// Creating input stream for reading in the frequency file

		BinaryIn  in  = new BinaryIn(freqFile);
		String str = new String();

        while (!in.isEmpty()) {
            str += in.readChar();
        }


		// Counting number of lines in freq.txt

		int count = 0;
		for (int i = 0; i < str.length(); i++) {
			if (str.charAt(i) == ':') {
				count++;
			}
		}


		// Creating HashMap and populating with characters as keys and frequencies as values

		HashMap<String, String> freqMap = new HashMap<String, String>();

		for (int i = 0; i < count; i++) {

			String binaryChar = "";
			for (int j = 0; j < 8; j++) {
				binaryChar += str.charAt(j);
			}
			char temp = (char) Integer.parseInt(binaryChar, 2);
			String charKey = Character.toString(temp);
			int k = 9;
			String freqValue = "";
			while (str.charAt(k) != '\n') {
				freqValue += str.charAt(k);
				k++;
			}

			if (i != count-1) {
				str = str.substring(k+1);
			}

			freqMap.put(charKey, freqValue);

		}


		// Creating arrays for frequency of each character, ordered by index

		Object[] fArray = freqMap.values().toArray();
		String[] freqArray = Arrays.copyOf(fArray, fArray.length, String[].class);
		Object[] cArray = freqMap.keySet().toArray();
		String[] charArray = Arrays.copyOf(cArray, cArray.length, String[].class);


		// Creating a min-heap using freqText, where each element is of type huffmanNode, containing a character and its corresponding frequency

		PriorityQueue<huffmanNode> freqHeap = new PriorityQueue<huffmanNode>(freqMap.size(), new MyComparator());

		for (int i = 0; i < freqMap.size(); i++) {

			huffmanNode n = new huffmanNode();

			n.c = charArray[i].charAt(0);
			n.freq = Integer.parseInt(freqArray[i]);
			n.left = null;
			n.right = null;

			freqHeap.add(n);

		}


		// Performing huffman encoding on the min-heap

		huffmanNode root = null;

		while (freqHeap.size() > 1) {
			
			huffmanNode min1 = freqHeap.peek();
			freqHeap.poll();
			huffmanNode min2 = freqHeap.peek();
			freqHeap.poll();
			huffmanNode sum = new huffmanNode();
			sum.freq = min1.freq + min2.freq;
			sum.left = min1;
			sum.right = min2;
			root = sum;
			freqHeap.add(root);

		}

		// EXTRA CREDIT: Printing huffman tree

		System.out.println("Huffman Tree created during decoding: \n");
		printNodeRight(root,0);


		// Reading in encoded file and using it to decode the huffman tree and write its values to the output file

		BinaryIn encodedInput = new BinaryIn(inputFile);
		String encodedString = new String();

		// Creating the encoded string
        while (!encodedInput.isEmpty()) {
			if (encodedInput.readBoolean()) {
				encodedString += "1";
			}
			else {
				encodedString += "0";
			}
        }

		huffmanNode curr = freqHeap.peek();
		String output = "";


		// Using the encoded string to write to the output string

		for (int i = 0; i < encodedString.length(); i++) {

			if (encodedString.charAt(i) == '1') {
				curr = curr.right;
			}
			else {
				curr = curr.left;
			}

			if (curr.left == null && curr.right == null) {
				output += curr.c;
				curr = freqHeap.peek();
			}

		}

		
		// Creating decoded output file using the output string

		BinaryOut decodedOutput = new BinaryOut(outputFile);
		decodedOutput.write(output);
		decodedOutput.flush();

	}


	/** MAIN METHOD */
   public static void main(String[] args) {

		HuffmanSubmit  huffman = new HuffmanSubmit();
		Scanner s = new Scanner(System.in);

		System.out.println("Enter the letter 'e' to encode a file or the letter 'd' to decode a file: ");
		String cmd = s.next();
		String cmd1;
		String cmd2;
		String cmd3;

		while (!cmd.equals("e") && !cmd.equals("d")) {
			System.out.println("Invalid command entered. Please enter a valid command: ");
			cmd = s.next();
		}

		if (cmd.equals("e")) {
			System.out.println("Please enter the name of the file to be encoded: ");
			cmd1 = s.next();
			System.out.println("Please enter the name of the encoded file: ");
			cmd2 = s.next();
			System.out.println("Please enter the name of the frequency file: ");
			cmd3 = s.next();
			System.out.println();
			huffman.encode(cmd1, cmd2, cmd3);
		}
		else {
			System.out.println("Please enter the name of the file to be decoded: ");
			cmd1 = s.next();
			System.out.println("Please enter the name of the decoded file: ");
			cmd2 = s.next();
			System.out.println("Please enter the name of the frequency file: ");
			cmd3 = s.next();
			System.out.println();
			huffman.decode(cmd1, cmd2, cmd3);
		}

   }

}
