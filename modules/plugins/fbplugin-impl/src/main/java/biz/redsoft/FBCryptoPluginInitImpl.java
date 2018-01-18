package biz.redsoft;

import org.apache.log4j.BasicConfigurator;
import org.firebirdsql.cryptoapi.cryptopro.exception.CryptoException;
import org.firebirdsql.gds.impl.wire.auth.AuthCryptoPlugin;

/**
 * Implementation of the сrypto plugin initialization interface
 *
 * @author <a href="mailto:vasiliy.yashkov@red-soft.ru">Vasiliy Yashkov</a>
 */
public class FBCryptoPluginInitImpl implements IFBCryptoPluginInit {
    @Override
    public void init() throws Exception {
        BasicConfigurator.configure();
        try {
            AuthCryptoPlugin.register(new org.firebirdsql.cryptoapi.AuthCryptoPluginImpl());
        } catch (CryptoException e) {
            throw new Exception(e.getMessage());
        }
    }
}
