package com.hllbr.travelbook.Model;

import java.io.Serializable;

public class Place implements Serializable {
    //profesyonel yazılımlarda properties yapıarlımı eğer bu şekilde genel amaçlı kullanmak istiyorsam başına public yazarak güvenliğini arttırmam gerek

    public String name ;
    public Double latitude ;
    public Double longitude ;
    //burada Double olarak ifade etmiş olsamda SQLite içerisinde String olarakta tutabilirim .

    //şimdi bu yapıyı kullanmak için ihtiyacım olan objelerden bir constructor yazmam gerekiyor.

    public Place(String name,Double latitude,Double longitude){
        this.name = name ;
        this.latitude = latitude ;
        this.longitude = longitude ;

    }
}
