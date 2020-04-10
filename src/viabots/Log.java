package viabots;

public class Log {
public static void soutWTime(String s ) {
}//System.out.println(milisToString(System.currentTimeMillis())+" "+ s);
//}
public static void soutS2(String s ) {}
    public static void sout(String s ) {}
    public static void soutWTime2(String s ){
        System.out.println(milisToString(System.currentTimeMillis())+" "+ s);
    }

static String milisToString(long durationInMillis){


    long millis = durationInMillis % 1000;
    long second = (durationInMillis / 1000) % 60;
    long minute = (durationInMillis / (1000 * 60)) % 60;
    long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

return String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);


}

}
