package viabots;

public enum BoxType {
    A, B, C;

public static BoxType fromChar(char c){
        switch (c){
            case 'A':return A;
            case 'B':return B;
            case 'C':return C;
            default:return null;
        }
    }
}
