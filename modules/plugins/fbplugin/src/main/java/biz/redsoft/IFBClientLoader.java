package biz.redsoft;

public interface IFBClientLoader {

    Object load(int driverVersion);

    void dispose(Object fbclient);

}
