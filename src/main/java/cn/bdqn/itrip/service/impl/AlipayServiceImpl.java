package cn.bdqn.itrip.service.impl;

import cn.bdqn.itrip.pojo.AlipayOrderParam;
import cn.bdqn.itrip.pojo.AlipayParam;
import cn.bdqn.itrip.service.AlipayService;
import cn.bdqn.itrip.utils.ToJsonUtil;
import cn.bdqn.itrip.utils.UuidUtils;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Service
public class AlipayServiceImpl implements AlipayService {

    @Value("${ali_return_url}")
    private String ali_return_url;

    @Value("${ali_notify_url}")
    private String ali_notify_url;

    @Value("${product_no}")
    private String product_no;

    @Value("${time_express}")
    private String time_express;

    @Value("${url}")
    private String url;

    @Value("${appid}")
    private String appid;

    @Value("${private_key}")
    private String private_key;

    @Value("${ali_public_key}")
    private String ali_public_key;

    @Value("${sign_type}")
    private String sign_type;

    /*@Resource
    private CashLogMapper cashLogMapper;

    @Resource
    private CashMapper cashMapper;

    @Resource
    private OrderMapper orderMapper;*/
    public static final String TRADE_SUCCESS = "TRADE_SUCCESS"; //支付成功标识
    public static final String TRADE_CLOSED = "TRADE_CLOSED";//交易关闭

    @Override
    public String alipay(AlipayParam param) {
        // 从 session 中获取用户
        /*UserData userData = UserContext.getUserData();
        if (userData == null || userData.getUserId() == null) {
            return WebUtils.buildPage("登录失效, 请重新登录");
        }
        if (!BusinessTypeConstant.USER_CATEGORY_SELLER.equals(userData.getUserCategory())) {
            log.info("卖家用户才可以进行支付");
            return WebUtils.buildPage("卖家用户才可以进行支付");
        }*/
        // 订单已支付
        /*Cash cash = cashMapper.getCashByOrderId(param.getOrderId());
        if (cash != null && "3".equals(cash.getStatus())) {
            log.info("订单已支付");
            return WebUtils.buildPage("订单已支付");
        }*/
        /*String merchantOrderNo;
        Boolean isRetry = false;
        if (cash != null && new Date(System.currentTimeMillis() - 7800000).before(cash.getCreateTime())) {
            merchantOrderNo = cash.getMerchantOrderNo();
            isRetry = true;
        } else {
            merchantOrderNo = param.getOrderId() + new SimpleDateFormat("HHmmss").format(new Date());
        }
        String urlEncodeOrderNum = "";
        try {
            urlEncodeOrderNum = URLEncoder.encode(merchantOrderNo, "UTF-8");//公告参数订单号
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        //向支付宝发送支付请求
        AlipayClient alipayClient = new DefaultAlipayClient(url, appid
                , private_key, "json", "UTF-8"
                , ali_public_key, sign_type);
        //创建Alipay支付请求对象
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setReturnUrl(ali_return_url); //同步通知url
        request.setNotifyUrl(ali_notify_url);//异步通知url
        AlipayOrderParam alipayOrderParam = new AlipayOrderParam();
        alipayOrderParam.setOut_trade_no(param.getOrderId());//唯一标识 商户订单号
        alipayOrderParam.setProduct_code(product_no);    //销售产品码
        alipayOrderParam.setSubject(param.getOrderName());  //订单名称
//        alipayOrderParam.setSubject("酒店房间订单");   //订单名称
        alipayOrderParam.setTotal_amount(param.getAmount());   //总金额
        alipayOrderParam.setTimeout_express(time_express);    //该笔订单允许的最晚付款时间，逾期将关闭交易
        alipayOrderParam.setPassback_params("test");   //公共校验参数
        /*Order order = new Order();
        order.setOrderNo(param.getOrderId());
        order.setUserId(1);
        order.setOrderType(1);*/
        request.setBizContent(ToJsonUtil.toJson(alipayOrderParam));//设置参数
        String webForm = "";//输出页面的表单
        try {
            webForm = alipayClient.pageExecute(request).getBody(); //调用SDK生成表单
        } catch (Exception e) {
            e.printStackTrace();
            /*log.info("支付请求发送失败");
            return WebUtils.buildPage("支付请求发送失败,请联系我们客服协助处理");*/
        }

        // 记录下发起付款
        /*if (isRetry) {
            cashLogMapper.add(merchantOrderNo, "RETRY", JSON.toJSONString(alipayOrderParam), new Date());
        } else {
            cashLogMapper.add(merchantOrderNo, "ADD", JSON.toJSONString(alipayOrderParam), new Date());
        }*/
        // 创建一条支付状态记录
//        cashMapper.add(param.getOrderId(), merchantOrderNo, new Date());
        return webForm;
    }

    @Override
    public String synchronous(HttpServletRequest request) {

        Map<String, String> parameters = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
//        log.info("支付宝同步参数", requestParams);
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            parameters.put(key, valueStr);
        }
        //记录日志
        String merchantOrderNo = request.getParameter("out_trade_no");//商户订单号
//        cashLogMapper.add(request.getParameter("out_trade_no"), "SYNCHRONOUS", ToJsonUtil.toJson(parameters), new Date());
        return "<html>\n" +
                "<head>\n" +
                "<script type=\"text/javascript\"> function load() { window.close(); } </script>\n" +
                "</head>\n" +
                "<body onload=\"" + "load()\"> </body>\n" +
                "</html>";
    }

    @Override
    public void notify(HttpServletRequest request, HttpServletResponse response) {
        /*
        //接收参数进行校验
        Map<String, String> parameters = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
            String key = entry.getKey();
            String[] values = entry.getValue();
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            parameters.put(key, valueStr);
        }
//        log.info("parameters is [parameters={}]", parameters);
        String appId = request.getParameter("app_id");//appid
        String merchantOrderNo = request.getParameter("out_trade_no");//商户订单号

        String orderId = cashMapper.getCashByMerchantOrderNo(merchantOrderNo).getOrderId();
        if (orderId == null) {
            log.error("orderId is null");
            ReturnData.fail(ReturnCode.SERVER_EXCEPTION);
        }
        log.info("orderId: {}", orderId);
        String payState = request.getParameter("trade_status");//交易状态
        String encodeOrderNum = null;
        cashLogMapper.add(request.getParameter("out_trade_no"), "NOTIFY", JSON.toJSONString(parameters), new Date());
        try {
            encodeOrderNum = URLDecoder.decode(request.getParameter("passback_params"), "UTF-8");
            log.info("encodeOrderNum is [encodeOrderNum={}]", encodeOrderNum);

            boolean signVerified;
            signVerified = AlipaySignature.rsaCheckV1(parameters, ali_public_key, "UTF-8", sign_type);//验证签名
            log.info("signVerified is [signVerified={}]", signVerified);
            if (signVerified) { //通过验证
                log.info("payState: {}", payState);
                if (payState.equals(TRADE_SUCCESS)) {
                    //判断订单号与插入的订单号是否一样
                    if (merchantOrderNo.equals(encodeOrderNum) == false || appid.equals(appId) == false) {
                        log.info("vali failure");
                        cashMapper.update(merchantOrderNo, 4);

                        response.getOutputStream().print("failure");
                        return;
                    }
                    cashMapper.update(merchantOrderNo, 3);
                    orderMapper.afterPay(orderId
                    );
                    response.getOutputStream().print("success");
                    return;
                } else if (payState.equals(TRADE_CLOSED)) { //交易关闭
                    cashMapper.update(merchantOrderNo, 7);
                } else {
                    cashMapper.update(merchantOrderNo, 4);
                    response.getOutputStream().print("failure");
                    return;
                }
            } else {
                //签名校验失败更状态
                cashMapper.update(merchantOrderNo, 4);
                response.getOutputStream().print("failure");
                return;
            }
            log.info("encodeOrderNum is  [encodeOrderNum={}]", encodeOrderNum);
            cashMapper.update(merchantOrderNo, 4);
            response.getOutputStream().print("failure");
            return;
        } catch (AlipayApiException e) {
            log.error(e.getErrMsg());
            throw new RuntimeException("调用支付宝接口发生异常");
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
            throw new RuntimeException("URLDecoderf发生异常");
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException("IO发生异常");
        }

    }*/
    }
}
