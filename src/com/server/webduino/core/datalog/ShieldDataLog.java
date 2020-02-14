package com.server.webduino.core.datalog;


import com.server.webduino.core.Shield;


public class ShieldDataLog extends DataLog {

    public boolean open = false;
    public String tableName = "shielddatalog";
    private int shieldid;

    public ShieldDataLog(int shieldid) {
        super();
        this.shieldid = shieldid;
    }

    @Override
    public String getSQLInsert(String event, Object object) {

        Shield shield = (Shield) object;
        String sql;
        sql = "INSERT INTO " + tableName + " (shieldid, date, event) VALUES ("
                + shield.id + ","  + getStrDate() + ",\"" + event + "\");";
        return sql;
    }

}
