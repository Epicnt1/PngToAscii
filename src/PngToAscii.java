import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Scanner;

public class PngToAscii {

    private static int[][] readImageToArray(BufferedImage image) {

        int width = image.getWidth();
        int height = image.getHeight();
        byte[] pixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        boolean hasAlphaChannel = image.getAlphaRaster() != null;
        int[][] result = new int[height][width];

        if (hasAlphaChannel) {
            int pixelLength = 4;
            for (int col = 0, row = 0, pixel = 0; pixel + 3 < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += (((int) pixels[pixel] & 0xff) << 24); // alpha
                argb += ((int) pixels[pixel + 1] & 0xff); // blue
                argb += (((int) pixels[pixel + 2] & 0xff) << 8); // green
                argb += (((int) pixels[pixel + 3] & 0xff) << 16); // red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        } else {
            int pixelLength = 3;
            for (int col = 0, row = 0, pixel = 0; pixel + 2 < pixels.length; pixel += pixelLength) {
                int argb = 0;
                argb += ((int) pixels[pixel] & 0xff); //blue
                argb += (((int) pixels[pixel + 1] & 0xff) << 8); //green
                argb += (((int) pixels[pixel + 2] & 0xff) << 16); //red
                result[row][col] = argb;
                col++;
                if (col == width) {
                    col = 0;
                    row++;
                }
            }
        }
        return result;
    }

    private static int averageChunkValues(int coordsx, int coordsy, int chunkSize, int[][] array) {
        int total = 0;
        for (int i = coordsx; i < chunkSize + coordsx; i++) {
            for (int j = coordsy; j < chunkSize + coordsy; j++) {
                total += (array[i][j] & 0x000000ff);
                total += ((array[i][j] & 0x0000ff00) >> 8);
                total += ((array[i][j] & 0x00ff0000) >> 16);
                total += ((array[i][j] & 0xff000000) >> 24);
            }
        }
        return (total / (chunkSize * chunkSize));
    }

    private static void printArrayInAscii(int[][] array, int chunkSize) {
        for (int i = 0; i + chunkSize < array.length; i += chunkSize) {
            for (int j = 0; j + chunkSize < array[i].length; j += chunkSize) {
                int ACV = averageChunkValues(i, j, chunkSize, array);
                if (ACV < 250) System.out.print("...");
                else if (ACV >= 250 && ACV < 400) System.out.print("///");
                else if (ACV >= 400 && ACV < 510) System.out.print("***");
                else if (ACV >= 510 && ACV < 600) System.out.print("888");
                else if (ACV >= 600 && ACV < 700) System.out.print("%%%");
                else if (ACV >= 700 && ACV <= 1020) System.out.print("$$$");
            }
            System.out.println();
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner scan = new Scanner(System.in);
        System.out.println("Enter file path:");
        String filepath = scan.nextLine();
        System.out.println("Enter chunk size:");
        int chunkSize = scan.nextInt();
        scan.close();
        System.setOut(new PrintStream(System.getProperty("user.home") + "\\Downloads\\Out.txt"));
        BufferedImage newImage = ImageIO.read(new File(filepath));
        int[][] data = readImageToArray(newImage);
        printArrayInAscii(data, chunkSize);
    }
}
