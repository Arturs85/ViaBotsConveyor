package viabots.behaviours;

public enum ConeType {A, B, C, D;

public static ConeType fromString(char c){
    switch (c){
        case 'a':
            return A;
        case 'b':
            return B;
        case 'c':
            return C;
        case 'd':
            return D;
        default:return null;

    }
}
}
