// Import any package as required


public class HuffmanSubmit implements Huffman {
  
	// Feel free to add more methods and variables as required. 
 
	public void encode(String inputFile, String outputFile, String freqFile){
		// TODO: Your code here
   }


   public void decode(String inputFile, String outputFile, String freqFile){
		// TODO: Your code here
   }




   public static void main(String[] args) {
      Huffman  huffman = new HuffmanSubmit();
		huffman.encode("ur.jpg", "ur.enc", "freq.txt");
		huffman.decode("ur.enc", "ur_dec.jpg", "freq.txt");
		// After decoding, both ur.jpg and ur_dec.jpg should be the same. 
		// On linux and mac, you can use `diff' command to check if they are the same. 
   }

}
