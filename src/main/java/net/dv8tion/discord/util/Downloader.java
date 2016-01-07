package net.dv8tion.discord.util;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

public class Downloader
{
    public static String webpage(String urlText)
    {
        String webpageText = "";
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;

        try
        {
            url = new URL(urlText);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null)
            {
                webpageText += line;
            }
        }
        catch (MalformedURLException mue)
        {
            mue.printStackTrace();
            return null;
        }
        catch (IOException ioe)
        {
            ioe.printStackTrace();
            return null;
        }
        finally
        {
            try
            {
                if (is != null) is.close();
            }
            catch (IOException ioe)
            {
                // nothing to see here
            }
        }
        return webpageText;
    }

    public static BufferedImage image(String urlText)
    {
        try
        {
            BufferedImage image = ImageIO.read(new URL(urlText));
            return image;
        }
        catch (IOException e)
        {
            System.out.printf("Could not find image at URL: %s\n", urlText);
            e.printStackTrace();
            return null;
        }
    }

    public static File file(String urlText, String fileName) throws IOException
    {
        File file = new File(fileName);
        file.getParentFile().mkdirs();

        URL link = new URL(urlText); //The file that you want to download

        //Code to download
        InputStream in = new BufferedInputStream(link.openStream());
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n = 0;
        while (-1!=(n=in.read(buf)))
        {
            out.write(buf, 0, n);
        }
        out.close();
        in.close();
        byte[] response = out.toByteArray();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(response);
        fos.close();
        return file;
    }
}
