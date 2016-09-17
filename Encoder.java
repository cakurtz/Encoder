import java.io.*;
import java.util.*;

public class Encoder {
	static String[] coding;
	static String[] doubleCoding;
	static String[] doubleSymbols;
	static Map<String, String> dblSymbolToCoding;
	
	public static void main(String[] args) {
		File file;
		File testText;
		File enc1;
		File dec1;
		int[] freq;
		int freqSum;
		int numOfChars;
		int bitsUsed;
		double entropy;
		double efficiency;
		double difference;
		double percentage;
		
		file = new File(args[0]);
		numOfChars = Integer.parseInt(args[1]);
		freq = new int[27];
		
		// Getting probabilities from file and calculating entropy
		extractProb(file, freq);
		freqSum = findSum(freq);
		entropy = calcEntropy(freq, freqSum);
		
		// Creating encoding with Huffman
		coding = new String[freq.length];
		StringBuffer curr = new StringBuffer();
		HuffmanTree current = HuffmanCode.buildTree(freq, false);
		System.out.println("Char Frequency Encoding");
		HuffmanCode.printCodes(current, curr, freqSum);
		
		// Encoding of Single Letters
		testText = new File("testText.out");
		enc1 = new File("testText.enc1");
		enc1.delete();
		enc1 = new File("testText.enc1");
		writeSingleOutput(testText, freq, numOfChars, freqSum);
		encodeSingle(testText, enc1, false);
		
		// Decoding of Single Letters
		dec1 = new File("testText.dec1");
		dec1.delete();
		dec1 = new File("testText.dec1");
		bitsUsed = decodeSingle(enc1, dec1, false);
		efficiency = (double) bitsUsed / (double) numOfChars;
		System.out.println();
		System.out.println("Entropy: " + entropy);
		System.out.println("Efficiency of Coding: " + efficiency);
		difference = efficiency - entropy;
		percentage = (double) difference / (double) entropy * (double) 100;
		System.out.println("Percentage Difference: " + percentage);
		System.out.println();
		
		// Double Encoding
		createDouble(testText, freq, freqSum, numOfChars);
	}
	
	private static void createDouble(File test, int[] freq, int sum, int numOfChars) {
		File enc2;
		File dec2;
		int[] doubleChars;
		int size;
		int total;
		int doubleIndex;
		int bitsUsed;
		double entropy;
		double efficiency;
		double difference;
		double percentage;
		
		size = 0;
		for(int i = 0; i < freq.length; i++)
			if(freq[i] != -1)
				size++;
		total = sum * sum;
		doubleIndex = 0;
		doubleChars = new int[total];
		doubleCoding = new String[total];
		doubleSymbols = new String[total];
		int index = 0;
		for(int i = 0; i < size; i++) 
			for(int j = 0; j < size; j++) {
				doubleChars[index] = freq[i] * freq[j];
				doubleSymbols[index] = (char)(i + 65) + "" + (char)(j + 65);
				index++;
			}
		for(int i = 0; i < doubleChars.length; i++)
			doubleIndex += doubleChars[i];
		HuffmanTree current = HuffmanCode.buildTree(doubleChars, true);
		StringBuffer curr = new StringBuffer("");
		dblSymbolToCoding = new HashMap<String, String>();
		System.out.println("Char Frequency Encoding");
		HuffmanCode.printDoubleCodes(current, curr, doubleIndex);
		
		enc2 = new File("testText.enc2");
		enc2.delete();
		enc2 = new File("testText.enc2");
		encodeSingle(test, enc2, true);
		
		dec2 = new File("testText.dec2");
		dec2.delete();
		dec2 = new File("testText.dec2");
		bitsUsed = decodeSingle(enc2, dec2, true);
		
		entropy = calcEntropy(doubleChars, doubleIndex);
		efficiency = (double) bitsUsed / (double) numOfChars;
		System.out.println();
		System.out.println("Entropy: " + entropy);
		System.out.println("Efficiency of Coding: " + efficiency);
		difference = efficiency - entropy;
		percentage = (double) difference / (double) entropy * (double) 100;
		System.out.println("Percentage Difference: " + percentage);
		System.out.println();
	}
	
	private static int decodeSingle(File input, File output, boolean isMulti) {
		Scanner fs;
		FileWriter outWrite;
		BufferedWriter outBuff;
		String cur;
		String print;
		int bitsUsed;
		
		bitsUsed = 0;
		print = "";
		try {
			fs = new Scanner(input);
			while(fs.hasNextLine()) {
				cur = fs.nextLine();
				for(int i = 0; i < cur.length(); i++)
					bitsUsed++;
				outWrite = new FileWriter(output, true);
				outBuff = new BufferedWriter(outWrite);
				if(isMulti) {
					for(Map.Entry<String, String> entry : dblSymbolToCoding.entrySet())
						if(entry.getValue().equals(cur))
							print = entry.getKey();
				}
				else {
					for(int j = 0; j < coding.length; j++)
						if(cur.equals(coding[j]))
							print = Character.toString((char) (j + 65));
				}
				outBuff.write(print);
				outBuff.close();
				outWrite.close();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return bitsUsed;
	}
	
	private static void encodeSingle(File input, File output, boolean isMulti) {
		Scanner fs;
		FileWriter outWrite;
		BufferedWriter outBuff;
		char cur;
		String curDbl;
		int index;
		
		cur = ' ';
		curDbl = "";
		try {
			fs = new Scanner(input);
			fs.useDelimiter("");
			while(fs.hasNext()) {
				if(isMulti){
					curDbl = Character.toString(fs.next().charAt(0));
					if(fs.hasNext())
						curDbl += "" + Character.toString(fs.next().charAt(0));
					else
						curDbl += 'A';
					outWrite = new FileWriter(output, true);
					outBuff = new BufferedWriter(outWrite);
					outBuff.write(dblSymbolToCoding.get(curDbl) + "\n");
					outBuff.close();
					outWrite.close();
				}
				else {
					cur = fs.next().charAt(0);
					outWrite = new FileWriter(output, true);
					outBuff = new BufferedWriter(outWrite);
					index = (int)(cur) - 65;
					outBuff.write(coding[index] + "\n");
					outBuff.close();
					outWrite.close();
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void writeSingleOutput(File testText, int[] freq, int numOfChars, int freqSum) {
		File output = testText;
		output.delete();
		output = testText;
		FileWriter outWrite;
		BufferedWriter outBuff;
		int randNum;
		int temp;
		int oldTemp;
		char[] probabilities;
		
		temp = 0;
		oldTemp = temp;
		probabilities = new char[freqSum];
		for(int index = 0; index < freq.length; index++) {
			oldTemp = temp;
			temp += freq[index];
			for(int newIndex = oldTemp; newIndex < temp; newIndex++)
				probabilities[newIndex] = (char) (index + 65);
		}
		
		for(int i = 0; i < numOfChars; i++) {
			randNum = (int)(Math.random() * freqSum);
			try {
				outWrite = new FileWriter(output, true);
				outBuff = new BufferedWriter(outWrite);
				try {
					outBuff.write(Character.toString(probabilities[randNum]));
					outBuff.close();
				} 
				catch (IOException e) {
					System.out.println("Print to output file failed");
				}
				outWrite.close();
			} 
			catch (IOException e) {
				System.out.println("Writer failure.");
			}
		}
	}
	
	private static double calcEntropy(int[] holder, int sum) {
		double result = 0;
		
		for(int i = 0; i < holder.length; i++) 
			if(holder[i] > 0)
				result += (double)holder[i] / (double)sum * Math.abs(Math.log((double)holder[i] / (double)sum) / Math.log(2));
		return result;
	}
	
	private static int findSum(int[] holder) {
		int sum = 0;
		
		for(int i = 0; i < holder.length; i++)
			if(holder[i] != -1)
				sum += holder[i];
		return sum;
	}
	
	private static void extractProb(File f, int[] holder) {
		int index = 0;
		
		try {
			Scanner fs = new Scanner(f);
			while(fs.hasNextInt()) {
				int temp = fs.nextInt();
				holder[index] = temp;
				index++;
			}
			fs.close();
			int newIndex = 0;
			for(int i = index; i < holder.length; i++) {
				holder[index + newIndex] = -1;
				newIndex++;
			}
		} catch (FileNotFoundException e) {
			System.out.println("No file found");
			e.printStackTrace();
		}
	}
	
	abstract static class HuffmanTree implements Comparable<HuffmanTree> {
	    public final int frequency; // the frequency of this tree
	    public HuffmanTree(int freq) { frequency = freq; }
	 
	    // compares on the frequency
	    public int compareTo(HuffmanTree tree) {
	        return frequency - tree.frequency;
	    }
	}
	 
	static class HuffmanLeaf extends HuffmanTree {
	    public final String value; // the string this leaf represents
	 
	    public HuffmanLeaf(int freq, String val) {
	        super(freq);
	        value = val;
	    }
	}
	 
	static class HuffmanNode extends HuffmanTree {
	    public final HuffmanTree left, right; // subtrees
	 
	    public HuffmanNode(HuffmanTree l, HuffmanTree r) {
	        super(l.frequency + r.frequency);
	        left = l;
	        right = r;
	    }
	}
	 
	public static class HuffmanCode {
	    // input is an array of frequencies, indexed by character code
	    public static HuffmanTree buildTree(int[] charFreqs, boolean isMult) {
	        PriorityQueue<HuffmanTree> trees = new PriorityQueue<HuffmanTree>();
	        // initially, we have a forest of leaves
	        // one for each non-empty character
	        for (int i = 0; i < charFreqs.length; i++) {
	            if (charFreqs[i] > 0 && !isMult)
	                trees.offer(new HuffmanLeaf(charFreqs[i], Character.toString((char)(i + 65))));
	            else if (charFreqs[i] > 0 && isMult)
	            	trees.offer(new HuffmanLeaf(charFreqs[i], doubleSymbols[i]));
	            else
	            	continue;
	        }
	 
	        assert trees.size() > 0;
	        // loop until there is only one tree left
	        while (trees.size() > 1) {
	            // two trees with least frequency
	            HuffmanTree a = trees.poll();
	            HuffmanTree b = trees.poll();
	 
	            // put into new node and re-insert into queue
	            trees.offer(new HuffmanNode(a, b));
	        }
	        return trees.poll();
	    }
	 
	    public static void printCodes(HuffmanTree tree, StringBuffer prefix, int sum) {
	        assert tree != null;
	        if (tree instanceof HuffmanLeaf) {
	            HuffmanLeaf leaf = (HuffmanLeaf)tree;
	 
	            // print out character, frequency, and code for this leaf (which is just the prefix)
	            System.out.println(leaf.value + "\t" + leaf.frequency + "/" + sum + "\t" + prefix);
	            coding[leaf.value.charAt(0) - 65] = prefix.toString();
	 
	        } else if (tree instanceof HuffmanNode) {
	            HuffmanNode node = (HuffmanNode)tree;
	 
	            // traverse left
	            prefix.append('0');
	            printCodes(node.left, prefix, sum);
	            prefix.deleteCharAt(prefix.length()-1);
	 
	            // traverse right
	            prefix.append('1');
	            printCodes(node.right, prefix, sum);
	            prefix.deleteCharAt(prefix.length()-1);
	        }
	    }
	    
	    public static void printDoubleCodes(HuffmanTree tree, StringBuffer prefix, int sum) {
	        assert tree != null;
	        if (tree instanceof HuffmanLeaf) {
	            HuffmanLeaf leaf = (HuffmanLeaf)tree;
	 
	            // print out character, frequency, and code for this leaf (which is just the prefix)
	            System.out.println(leaf.value + "\t" + leaf.frequency + "/" + sum + "\t" + prefix);
	            dblSymbolToCoding.put(leaf.value, prefix.toString());
	 
	        } else if (tree instanceof HuffmanNode) {
	            HuffmanNode node = (HuffmanNode)tree;
	 
	            // traverse left
	            prefix.append('0');
	            printDoubleCodes(node.left, prefix, sum);
	            prefix.deleteCharAt(prefix.length()-1);
	 
	            // traverse right
	            prefix.append('1');
	            printDoubleCodes(node.right, prefix, sum);
	            prefix.deleteCharAt(prefix.length()-1);
	        }
	    }
	}
}
