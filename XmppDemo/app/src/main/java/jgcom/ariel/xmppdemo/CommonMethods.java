package jgcom.ariel.xmppdemo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

/**
 * Created by Ariel-17 on 11/7/2017.
 */

public class CommonMethods {

    Calendar mCalander;
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy");
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    public static CommonMethods instance;

    public CommonMethods()
    {
        mCalander = Calendar.getInstance();
    }

    public static CommonMethods getInstance()
    {
        if(instance==null)
        {
            instance=new CommonMethods();
        }
        return instance;
    }


    public String getCurrentDate() {
        //Date date=new Date();
        return dateFormat.format(mCalander.getTime());
    }

    public String getCurrenttime() {
        return timeFormat.format(mCalander.getTime());
    }

    public String getMsgID() {
        int min = 100;
        int max = 100000;
        Random random = new Random();
        return String.valueOf(random.nextInt(max - min) + 100);
    }
}
