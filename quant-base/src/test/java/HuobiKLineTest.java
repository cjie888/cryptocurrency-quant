
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiClientFactory;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiException;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiRestClient;
import com.cjie.cryptocurrency.quant.api.huobi.HuobiApiWSClient;
import com.cjie.cryptocurrency.quant.api.huobi.domain.HuobiKLineData;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSError;
import com.cjie.cryptocurrency.quant.api.huobi.domain.ws.HuobiWSKLineEvent;
import com.cjie.cryptocurrency.quant.api.huobi.misc.HuobiWSEventHandler;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class HuobiKLineTest {

    @Test
    public void test() throws HuobiApiException {
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiRestClient client = factory.newRestClient();
        List<HuobiKLineData> list = client.kline("btcusdt","1min",1);
        Assert.assertFalse(list.isEmpty());
        for (HuobiKLineData data: list){
            System.out.println("btc---" + data);
        }

        List<HuobiKLineData> list2 = client.kline("ethusdt","1min",1);
        Assert.assertFalse(list2.isEmpty());
        for (HuobiKLineData data: list2){
            System.out.println("eth---" + data);
        }
    }

    @Test(expected = HuobiApiException.class)
    public void testWithErrorSymbol() throws HuobiApiException{
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiRestClient client = factory.newRestClient();
        client.kline("btcusdt123","5min",10);
    }

    @Test
    public void wsTest() throws HuobiApiException, InterruptedException {
        HuobiApiClientFactory factory = HuobiApiClientFactory.newInstance();
        HuobiApiWSClient client = factory.newWSClient();

        client.kline("btcusdt", "5min", new HuobiWSEventHandler() {
            @Override
            public void handleKLine(HuobiWSKLineEvent event) {
                System.out.println(event);
            }

            @Override
            public void onError(HuobiWSError error) {
                System.err.println(error);
            }
        });

        Thread.sleep(1000 * 10);
    }
}
