
import com.alibaba.fastjson.JSON;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiException;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiWSClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiOrderBook;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiSymbol;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSDepthEvent;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class HuobiDepthTest {

    @Test
    public void wsTest() throws HuobiApiException {
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiWSClient client = factory.newWSClient();
        client.depth("eosbtc", "step0", new HuobiWSEventHandler() {
            @Override
            public void handleDepth(HuobiWSDepthEvent event) {
                System.out.println(event.toString());
            }
        });

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void restTest() throws HuobiApiException {
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiRestClient client = factory.newRestClient();
        List<String> currencies = client.currencys();
        System.out.println(JSON.toJSONString(currencies));
        List<HuobiSymbol> symbols = client.symbols();
        System.out.println(JSON.toJSONString(symbols));
        HuobiOrderBook ob = client.depth("eosusdt", "step4");
        Assert.assertNotNull( ob );
        System.out.println(ob);
    }

}
