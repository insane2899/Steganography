import lib.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.File;
import java.io.FileReader;



public class Steganography{

	private static String inputImageName,outputImageName,payloadName;
	private static long fileSize;
	private static byte[] secretData;

	public static void main(String[] args)throws IOException{
		try{
			switch(args[0]){
			case "encode":
				try{
					inputImageName = args[1];
					payloadName = args[2];
					outputImageName = args[3];
					File file = new File(payloadName);
					secretData = readSecret(file);
					PNMImage inputImage = ImageUtil.readImage(inputImageName);
					PNMImage outputImage = encodeImage(secretData,inputImage);
					ImageUtil.writeImage(outputImage,outputImageName,"P6");
					System.out.println("Encoded Image Created");
				}catch(Exception e){
					System.out.println("Encoding Failed: "+e);
					e.printStackTrace();
				}
				break;
			case "decode":
				try{
					inputImageName = args[1];
					payloadName = args[2];
					PNMImage inputImage = ImageUtil.readImage(inputImageName);
					secretData = readSecret(inputImage);
					File file = new File(payloadName);
					decodeData(secretData,file);
				}catch(Exception e){
					System.out.println("Decoding Failed: "+e);
				}
				break;
			}
		}catch(Exception e){
			System.out.println("Operation Failed: "+e);
		}
	}

	private static PNMImage encodeImage(byte[] secretData, PNMImage inputImage)throws Exception{
		int height = inputImage.getHeight();
		int width = inputImage.getWidth();
		int lengthData = secretData.length;
		if(height*width < lengthData*8){
			throw new Exception("Encoded data size is greater than Maximum data encoding capacity of Image");
		}
		PNMImage output = ImageUtil.getEmptyImage(height,width,inputImage.getOriginalFormat(),255);
		int firstSixteen = 0;
		int beg = 0,byteIndex = 0;;
		for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				int red = inputImage.getPixel(i,j,0);
				int green = inputImage.getPixel(i,j,1);
				int blue = inputImage.getPixel(i,j,2);
				if(firstSixteen < 16){
					int bit = (lengthData >> firstSixteen) & 1;
					int newBlue = encodeBit(bit,green,blue);
					output.setPixel(i,j,0,red);
					output.setPixel(i,j,1,green);
					output.setPixel(i,j,2,newBlue);
					firstSixteen++;
				}
				else{
					if(byteIndex == 8){
						beg++;
						byteIndex = 0;
					}
					if(beg==lengthData){
						output.setPixel(i,j,0,red);
						output.setPixel(i,j,1,green);
						output.setPixel(i,j,2,blue);
						continue;
					}
					int bit = (secretData[beg]>>byteIndex)&1;
					int newBlue = encodeBit(bit,green,blue);
					output.setPixel(i,j,0,red);
					output.setPixel(i,j,1,green);
					output.setPixel(i,j,2,newBlue);
					byteIndex++;
				}
			}
		}
		return output;
	}

	private static void decodeData(byte[] secretData,File file)throws IOException{
		OutputStream os = new BufferedOutputStream(new FileOutputStream(file),secretData.length);
		os.write(secretData,0,secretData.length);
		os.close();
	}

	private static int encodeBit(int bit,int green,int blue){
		if(((green^blue)&1) == bit){
			return blue;
		}
		else{
			if(blue==0){
				return blue+1;
			}
			else{
				return blue-1;
			}
		}
	}

	private static byte[] readSecret(File file)throws IOException{
		fileSize = file.length();
		InputStream is = new BufferedInputStream(new FileInputStream(file),(int)fileSize);
		byte[] secret = new byte[(int)fileSize];
		int bytesRead = is.read(secret);
		is.close();
		return secret;
	}

	private static byte[] readSecret(PNMImage inputImage)throws IOException{
		int secretSize = 0, height = inputImage.getHeight(), width = inputImage.getWidth();
		int countFirst = 0,countSize=0,countBit=0;
		byte[] secret = null;
		outer:for(int i=0;i<height;i++){
			for(int j=0;j<width;j++){
				int red = inputImage.getPixel(i,j,0);
				int green = inputImage.getPixel(i,j,1);
				int blue = inputImage.getPixel(i,j,2);
				if(countFirst < 16){
					secretSize = secretSize | (((green^blue)&1)<<countFirst);
					countFirst++;
				}
				else{
					if(secret==null){
						secret = new byte[secretSize];
					}
					if(countBit==8){
						countBit = 0;
						countSize++;
						if(countSize == secretSize){
							break outer;
						}
					}
					byte byt =(byte)(((green^blue)&1)<<countBit);
					//System.out.println(byt);
					secret[countSize] = (byte)(secret[countSize] | byt);
					countBit++;
				}
			}
		}
		return secret;
	}
}