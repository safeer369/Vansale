package com.teamayka.vansaleandmgmt.devtools;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Date;

public class UnCaughtException implements UncaughtExceptionHandler {
    private Context context;

    public UnCaughtException(Context ctx) {
        context = ctx;
    }

//    private StatFs getStatFs() {
//        File path = Environment.getDataDirectory();
//        return new StatFs(path.getPath());
//    }

//    private long getAvailableInternalMemorySize(StatFs stat) {
//        long blockSize = stat.getBlockSize();
//        long availableBlocks = stat.getAvailableBlocks();
//        return availableBlocks * blockSize;
//    }
//
//    private long getTotalInternalMemorySize(StatFs stat) {
//        long blockSize = stat.getBlockSize();
//        long totalBlocks = stat.getBlockCount();
//        return totalBlocks * blockSize;
//    }

//    private void addInformation(StringBuilder message) {
//        message.append("Locale: ").append(Locale.getDefault()).append('\n');
//        try {
//            PackageManager pm = context.getPackageManager();
//            PackageInfo pi;
//            pi = pm.getPackageInfo(context.getPackageName(), 0);
//            message.append("Version: ").append(pi.versionName).append('\n');
//            message.append("Package: ").append(pi.packageName).append('\n');
//        } catch (Exception e) {
//            Log.e("CustomExceptionHandler", "Error", e);
//            message.append("Could not get Version information for ").append(context.getPackageName());
//        }
//        message.append("Phone Model ").append(android.os.Build.MODEL).append('\n');
//        message.append("Android Version : ").append(android.os.Build.VERSION.RELEASE).append('\n');
//        message.append("Board: ").append(android.os.Build.BOARD).append('\n');
//        message.append("Brand: ").append(android.os.Build.BRAND).append('\n');
//        message.append("Device: ").append(android.os.Build.DEVICE).append('\n');
//        message.append("Host: ").append(android.os.Build.HOST).append('\n');
//        message.append("ID: ").append(android.os.Build.ID).append('\n');
//        message.append("Model: ").append(android.os.Build.MODEL).append('\n');
//        message.append("Product: ").append(android.os.Build.PRODUCT).append('\n');
//        message.append("Type: ").append(android.os.Build.TYPE).append('\n');
//        StatFs stat = getStatFs();
//        message.append("Total Internal memory: ").append(getTotalInternalMemorySize(stat)).append('\n');
//        message.append("Available Internal memory: ").append(getAvailableInternalMemorySize(stat)).append('\n');
//    }

    public void uncaughtException(Thread t, Throwable e) {
        try {
            StringBuilder report = new StringBuilder();
            Date curDate = new Date();
            report.append("Error Report collected on : ").append(curDate.toString()).append('\n');
            report.append("Stack:");
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            report.append(result.toString());
            printWriter.close();
            Log.e(UnCaughtException.class.getName(), "Error while sendError" + report);
            sendError(report);
        } catch (Throwable ignore) {
            Log.e(UnCaughtException.class.getName(), "Error while sending error.html e-mail", ignore);
        }
    }


    public void sendError(final StringBuilder errorContent) {
        Intent intent = new Intent(context, ExceptionActivity.class);
        intent.putExtra("exception_log", String.valueOf(errorContent));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        System.exit(0);
//        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        new Thread() {
//            @Override
//            public void run() {
//                Looper.prepare();
//                builder.setTitle("Sorry...!");
//                builder.create();
//                builder.setMessage(String.valueOf(errorContent));
//                builder.setNegativeButton("Copy",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                ClipboardManager manager = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
//                                ClipData clipData = ClipData.newPlainText("message", String.valueOf(errorContent));
//                                manager.setPrimaryClip(clipData);
//                                System.exit(1);
//                            }
//                        });
////                builder.setPositiveButton("Report",
////                        new DialogInterface.OnClickListener() {
////                            @Override
////                            public void onClick(DialogInterface dialog, int which) {
////                                Intent sendIntent = new Intent(Intent.ACTION_SEND);
////                                String subject = "Your App crashed! Fix it!";
////                                StringBuilder body = new StringBuilder("Yoddle");
////                                body.append('\n').append('\n');
////                                body.append(errorContent).append('\n').append('\n');
////                                // sendIntent.setType("text/plain");
////                                sendIntent.setType("message/rfc822");
////                                sendIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"coderzheaven@gmail.com"});
////                                sendIntent.putExtra(Intent.EXTRA_TEXT, body.toString());
////                                sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
////                                sendIntent.setType("message/rfc822");
////                                context1.startActivity(sendIntent);
////                                System.exit(0);
////                            }
////                        });
//                builder.show();
//                Looper.loop();
//            }
//        }.start();
    }
}