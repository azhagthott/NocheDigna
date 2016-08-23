package com.zecovery.android.nochedigna.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.zecovery.android.nochedigna.albergue.Albergue;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 20-07-16.
 */

public class LocalDataBaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = LocalDataBaseHelper.class.getName();

    private static final int DATABASE_VERSION = 9;
    private static final String DATABASE_NAME = "albergue.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMA = ", ";

    private static final String TABLE_ALBERGUE = "albergue";
    private static final String ALBERGUE_ID = "id";
    private static final String ALBERGUE_TIPO = "tipo";
    private static final String ALBERGUE_COBERTURA = "cobertura";
    private static final String ALBERGUE_CAMAS_DISPONIBLES = "camas_disponibles";
    private static final String ALBERGUE_LAT = "lat";
    private static final String ALBERGUE_LNG = "lng";
    private static final String ALBERGUE_REGION = "region";
    private static final String ALBERGUE_COMUNA = "comuna";
    private static final String ALBERGUE_DIRECCION = "direccion";
    private static final String ALBERGUE_TELEFONO = "telefono";
    private static final String ALBERGUE_EMAIL = "email";
    private static final String ALBERGUE_EJECUTOR = "ejecutor";

    private final String SQL_CREATE_ALBERGUES =
            "CREATE TABLE " + TABLE_ALBERGUE + " (" +
                    ALBERGUE_ID + TEXT_TYPE + " PRIMARY KEY" + COMA +
                    ALBERGUE_TIPO + TEXT_TYPE + COMA +
                    ALBERGUE_COBERTURA + TEXT_TYPE + COMA +
                    ALBERGUE_CAMAS_DISPONIBLES + TEXT_TYPE + COMA +
                    ALBERGUE_LAT + TEXT_TYPE + COMA +
                    ALBERGUE_LNG + TEXT_TYPE + COMA +
                    ALBERGUE_REGION + TEXT_TYPE + COMA +
                    ALBERGUE_COMUNA + TEXT_TYPE + COMA +
                    ALBERGUE_DIRECCION + TEXT_TYPE + COMA +
                    ALBERGUE_TELEFONO + TEXT_TYPE + COMA +
                    ALBERGUE_EMAIL + TEXT_TYPE + COMA +
                    ALBERGUE_EJECUTOR + TEXT_TYPE +
                    ");";

    private static final String SQL_DELETE_ALBERGUES = "DROP TABLE IF EXISTS " + TABLE_ALBERGUE;

    public LocalDataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(LOG_TAG, "onCreate: " + SQL_CREATE_ALBERGUES);
        db.execSQL(SQL_CREATE_ALBERGUES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ALBERGUES);
        onCreate(db);
    }

    /**
     * addAlbergue: Agrega un albergue a la db local, no afecta a la db en Firebase
     */
    public void addAlbergue(Albergue albergue) {

        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            values.put(ALBERGUE_ID, albergue.getIdAlbergue());
            values.put(ALBERGUE_TIPO, albergue.getTipo());
            values.put(ALBERGUE_COBERTURA, albergue.getCobertura());
            values.put(ALBERGUE_CAMAS_DISPONIBLES, albergue.getCamasDisponibles());
            values.put(ALBERGUE_LAT, albergue.getLat());
            values.put(ALBERGUE_LNG, albergue.getLng());
            values.put(ALBERGUE_REGION, albergue.getRegion());
            values.put(ALBERGUE_COMUNA, albergue.getComuna());
            values.put(ALBERGUE_DIRECCION, albergue.getDireccion());
            values.put(ALBERGUE_TELEFONO, albergue.getTelefonos());
            values.put(ALBERGUE_EMAIL, albergue.getEmail());
            values.put(ALBERGUE_EJECUTOR, albergue.getEjecutor());

            Log.d(LOG_TAG, "addAlbergue: " + values);

            db.insert(TABLE_ALBERGUE, null, values);
            db.close();

        } catch (SQLiteException sqlEx) {
            FirebaseCrash.log("SQLiteException: " + sqlEx);
            Log.d(LOG_TAG, "SQLiteException: " + sqlEx);
        } catch (Exception e) {
            FirebaseCrash.log("Exception: " + e);
            Log.d(LOG_TAG, "Exception: " + e);
        }

    }

    /**
     * Devuelve 1 albergue especificado por id
     *
     * @param id id del albergue que se quiere consultar
     */
    public Albergue getAlbergue(int id) {

        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.query(
                    TABLE_ALBERGUE, new String[]{
                            ALBERGUE_ID,
                            ALBERGUE_TIPO,
                            ALBERGUE_COBERTURA,
                            ALBERGUE_CAMAS_DISPONIBLES,
                            ALBERGUE_LAT,
                            ALBERGUE_LNG,
                            ALBERGUE_REGION,
                            ALBERGUE_COMUNA,
                            ALBERGUE_DIRECCION,
                            ALBERGUE_TELEFONO,
                            ALBERGUE_EMAIL,
                            ALBERGUE_EJECUTOR
                    },
                    ALBERGUE_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null,
                    null
            );

            if (cursor != null) {
                cursor.moveToFirst();
            }

            String idAlbergue = cursor.getString(0);
            String tipo = cursor.getString(1);
            String cobertura = cursor.getString(2);
            String camasDisponibles = cursor.getString(3);
            String lat = cursor.getString(4);
            String lng = cursor.getString(5);
            String region = cursor.getString(6);
            String comuna = cursor.getString(7);
            String direccion = cursor.getString(8);
            String telefonos = cursor.getString(9);
            String email = cursor.getString(10);
            String ejecutor = cursor.getString(11);
            return new Albergue(idAlbergue, region, comuna, tipo, cobertura, camasDisponibles, ejecutor, direccion, telefonos, email, lat, lng);

        } catch (SQLiteException sqlE) {
            Log.d(LOG_TAG, "getAlbergue: " + sqlE);
            FirebaseCrash.log("SQLiteException: " + sqlE);
            return null;
        } catch (Exception e) {
            Log.d(LOG_TAG, "getAlbergue: " + e);
            FirebaseCrash.log("Exception: " + e);
            return null;
        }
    }

    /**
     * getAlbergues: Devuelve listado con todos los albergues almacenados en db
     */
    public List<Albergue> getAlbergues() {

        List<Albergue> list = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_ALBERGUE;

        Log.d(LOG_TAG, "selectQuery: " + selectQuery);

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.d(LOG_TAG, "db: " + db);
        Log.d(LOG_TAG, "cursor: " + cursor);

        String idAlbergue = cursor.getString(0);
        String tipo = cursor.getString(1);
        String cobertura = cursor.getString(2);
        String camasDisponibles = cursor.getString(3);
        String lat = cursor.getString(4);
        String lng = cursor.getString(5);
        String region = cursor.getString(6);
        String comuna = cursor.getString(7);
        String direccion = cursor.getString(8);
        String telefonos = cursor.getString(9);
        String email = cursor.getString(10);
        String ejecutor = cursor.getString(11);

        if (cursor.moveToFirst()) {
            do {
                Albergue albergue = new Albergue();

                albergue.setIdAlbergue(idAlbergue);
                albergue.setTipo(tipo);
                albergue.setCobertura(cobertura);
                albergue.setCamasDisponibles(camasDisponibles);
                albergue.setLat(lat);
                albergue.setLng(lng);
                albergue.setRegion(region);
                albergue.setComuna(comuna);
                albergue.setDireccion(direccion);
                albergue.setTelefonos(telefonos);
                albergue.setEmail(email);
                albergue.setEjecutor(ejecutor);

                list.add(albergue);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return list;
    }

    /**
     * countAlbergues: Cuenta la cantidad de albergues almacenados en la db local
     */
    public int countAlbergues() {

        String countQuery = "SELECT * FROM " + TABLE_ALBERGUE;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int total = cursor.getCount();
        cursor.close();
        return total;
    }

    /**
     * updateAlbergue: actualiza la informacion de un albergue
     *
     * @param albergue : albergue que se actualizará
     */
    public int updateAlbergue(Albergue albergue) {

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(ALBERGUE_TIPO, albergue.getTipo());
        values.put(ALBERGUE_COBERTURA, albergue.getCobertura());
        values.put(ALBERGUE_CAMAS_DISPONIBLES, albergue.getCamasDisponibles());
        values.put(ALBERGUE_LAT, albergue.getLat());
        values.put(ALBERGUE_LNG, albergue.getLng());
        values.put(ALBERGUE_REGION, albergue.getRegion());
        values.put(ALBERGUE_COMUNA, albergue.getComuna());
        values.put(ALBERGUE_DIRECCION, albergue.getDireccion());
        values.put(ALBERGUE_TELEFONO, albergue.getTelefonos());
        values.put(ALBERGUE_EMAIL, albergue.getEmail());
        values.put(ALBERGUE_EJECUTOR, albergue.getEjecutor());

        return db.update(TABLE_ALBERGUE, values, ALBERGUE_ID + " = ?", new String[]{albergue.getIdAlbergue()});

    }

    /**
     * deleteAlbergue: borra de la db un albergue
     *
     * @param albergue : albergue que se borrará
     */
    public void deleteALbergue(Albergue albergue) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_ALBERGUE, ALBERGUE_ID + " = ?", new String[]{albergue.getIdAlbergue()});
        db.close();
    }

}
