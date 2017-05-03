package in.ceeq.alec.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage;

public class ImageUtils {

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        }
        return inSampleSize;
    }

    private static Bitmap getOrientationFixedBitmap(String filename, Bitmap bitmap) {
        try {
            final ExifInterface exif = new ExifInterface(filename);
            final int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            int angle;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    angle = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    angle = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    angle = 270;
                    break;
                default:
                    return bitmap;
            }
            Matrix matrix = new Matrix();
            matrix.postRotate(angle);
            return rotateImage(bitmap, angle);
        } catch (IOException e) {
            LogUtils.logException(e);
        }
        return bitmap;
    }

    public static String decodeSampledBitmap(String fromFilename, String toFilename, int reqWidth, int reqHeight,
                                             boolean fixOrientation) {
        FileInputStream inputStream;
        File file = new File(fromFilename);
        try {
            inputStream = new FileInputStream(file);
            if (fixOrientation) {
                FileOutputStream out = new FileOutputStream(toFilename);
                getOrientationFixedBitmap(fromFilename,
                        decodeSampledBitmapFromResourceMemOpt(inputStream, reqWidth, reqHeight))
                        .compress(
                                Bitmap.CompressFormat.JPEG, 70, out);
                out.close();
            } else {
                FileOutputStream out = new FileOutputStream(toFilename);
                decodeSampledBitmapFromResourceMemOpt(inputStream, reqWidth, reqHeight)
                        .compress(Bitmap.CompressFormat.JPEG, 70, out);
                out.close();
            }
            inputStream.close();
        } catch (Exception e) {
            LogUtils.logException(e);
            toFilename = "";
        }
        return toFilename;
    }

    private static Bitmap decodeSampledBitmapFromResourceMemOpt(InputStream inputStream, int reqWidth, int reqHeight) {
        try {
            byte[] byteArr = new byte[0];
            byte[] buffer = new byte[1024];
            int len;
            int count = 0;
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newBuffer = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newBuffer, 0, count);
                        byteArr = newBuffer;
                    }

                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }

            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            inputStream.close();
            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);

        } catch (IOException e) {
            LogUtils.logException(e);
            return null;
        } catch (RuntimeException e) {
            LogUtils.logException(e);
            return null;
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
