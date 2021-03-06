package vmax.hsedorms.api;

import android.content.Context;
import android.location.Location;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import vmax.hsedorms.R;

public class Places {
    public final static Place aeroport, tekstilshiki, strogino, myasnitskaya;
    public final static Place vavilova, izmailovo, st_basmannaya, shabolovskaya;
    public final static Place petrovka, paveletskaya, ilyinka, trehsvyat_b;
    public final static Place trehsvyat_m, hitra, gnezdo, ordynka;
    public final static Place dubki, odintsovo;
    public final static Place[] Edus, Dorms,  AllPlaces;


    static
    {
        aeroport = new Place("aeroport", "Кочновский проезд",  Pair.create(55.806661f, 37.541719f));
        tekstilshiki = new Place("tekstilshiki","Текстильщики", Pair.create(55.703758f, 37.726134f));

        strogino = new Place("strogino", "Строгино", Pair.create(55.803469f, 37.409846f));
        myasnitskaya = new Place("myasnitskaya", "Мясницкая",Pair.create(55.761345f,37.632969f));
        vavilova = new Place("vavilova", "Вавилова",Pair.create(55.704874f, 37.585359f));
        izmailovo = new Place("izmailovo", "Кирпичная улица",Pair.create(55.77873f, 37.733221f));
        st_basmannaya = new Place("st_basmannaya", "Старая Басманная",Pair.create(55.766734f, 37.663288f));
        shabolovskaya = new Place("shabolovskaya", "Шаболовская",Pair.create(55.72055f, 37.609245f));
        petrovka = new Place("petrovka", "Петровка ",Pair.create(55.762839f, 37.618516f));
        paveletskaya = new Place("paveletskaya", "Малая Пионерская",Pair.create(55.728358f, 37.63517f));
        ilyinka = new Place("ilyinka", "Ильинка",Pair.create(55.755864f, 37.628029f));
        trehsvyat_b = new Place("trehsvyat_b", "Большой Трёхсвятительский переулок",Pair.create(55.755368f, 37.646471f));
        trehsvyat_m = new Place("trehsvyat_m", "Малый Трёхсвятительский переулок",Pair.create(55.754091f, 37.646552f));
        hitra = new Place("hitra", "Хитровский переулок",Pair.create(55.753858f, 37.645519f));
        gnezdo = new Place("gnezdo", "Малый Гнездниковский переулок", Pair.create(55.76169f, 37.60611f));
        ordynka = new Place("ordynka", "Малая Ордынка", Pair.create(55.737512f, 37.626304f));

        dubki = new Place("dubki", "Дубки", Pair.create(55.660404f, 37.228889f));
        odintsovo = new Place("odintsovo", "Одинцово", Pair.create(55.669854f, 37.27968f));


        Edus = new Place[] {
                aeroport,tekstilshiki, ordynka, strogino,myasnitskaya,
                vavilova, izmailovo, st_basmannaya, shabolovskaya,
                petrovka, paveletskaya, ilyinka, trehsvyat_b,
                trehsvyat_m, hitra, gnezdo

        };
        Dorms = new Place[] {dubki,odintsovo};

        AllPlaces = new Place[] {
                aeroport,tekstilshiki, ordynka, strogino,myasnitskaya,
                vavilova, izmailovo, st_basmannaya, shabolovskaya,
                petrovka, paveletskaya, ilyinka, trehsvyat_b,
                trehsvyat_m, hitra, gnezdo, dubki, odintsovo
        };
    }

    public static boolean placesAreInSameGroup (Place place1, Place place2)
    {
        List<Places.Place> Edus = Arrays.asList(Places.Edus);
        List<Places.Place> Dorms = Arrays.asList(Places.Dorms);
        return ((Edus.contains(place1) && Edus.contains(place2))
                ||(Dorms.contains(place1) && Dorms.contains(place2)));

    }

    public static Place findPlaceByApiName(String apiName)
    {
        for (Place p : AllPlaces)
        {
            if (p.apiName.compareTo(apiName) == 0)
            {
                return p;
            }
        }
        return null;
    }

    public static Place getNearestPlace(Location currentLocation)
    {
        float distance = Float.MAX_VALUE;
        Place result = null;
        for (Place p: AllPlaces) {
            float new_distance = currentLocation.distanceTo(p.location);
            if (new_distance < distance)
            {
                result = p;
                distance = new_distance;
            }
        }
        return result;
    }

    public static class Place{
        public Location location;
        public String apiName;
        public String humanReadableName;

        public Place(String apiName, String humanReadableName, Pair<Float,Float> latlon) {
            this.humanReadableName = humanReadableName;
            this.apiName = apiName;
            this.location = new Location("");
            this.location.setLatitude(latlon.first);
            this.location.setLongitude(latlon.second);
        }


        @Override
        public String toString() {
            return humanReadableName;
        }

    }
}
