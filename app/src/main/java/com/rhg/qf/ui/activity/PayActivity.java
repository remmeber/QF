package com.rhg.qf.ui.activity;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rhg.qf.R;
import com.rhg.qf.adapter.PayItemAdapter;
import com.rhg.qf.application.InitApplication;
import com.rhg.qf.bean.AddressUrlBean;
import com.rhg.qf.bean.NewOrderBackModel;
import com.rhg.qf.bean.NewOrderBean;
import com.rhg.qf.bean.PayModel;
import com.rhg.qf.constants.AppConstants;
import com.rhg.qf.mvp.api.QFoodApi;
import com.rhg.qf.mvp.presenter.NewOrderPresenter;
import com.rhg.qf.pay.BasePayActivity;
import com.rhg.qf.pay.model.OrderInfo;
import com.rhg.qf.pay.model.PayType;
import com.rhg.qf.utils.AccountUtil;
import com.rhg.qf.utils.DecimalUtil;
import com.rhg.qf.utils.NetUtil;
import com.rhg.qf.utils.SizeUtil;
import com.rhg.qf.utils.ToastHelper;
import com.rhg.qf.widget.RecycleViewDivider;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.Bind;
import butterknife.OnClick;

/**
 * desc:付款页面 todo 跳到付款页面的数据都要有一个标志：来自购物车还是待付款页面。如果来自购物车，则要去掉购物车
 * 中的商品，标记为待付款或者完成；如果是来自待付款，则保留待付款或者完成；
 * author：remember
 * time：2016/5/28 16:14
 * email：1013773046@qq.com
 */
public class PayActivity extends BasePayActivity implements PayItemAdapter.PayItemClickListener {
    private final static String WX_MERCHANT_ID = "1374528702";
    private final static String WX_PRIVATE_KEY = //"shengzhoujiaze123456jiajiameishi"
            "fXRDOhhdr7rM0XWpKzFQry6pBjo0dllb";
    private final static String ALI_PARTNER = "2088422291942751";
    private final static String ALI_SELLER_ID = "18858558505";
    private final static String ALI_PRIVATE_KEY = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAMIwLMEyitvEEctRirBarCnmtDqcIYxl2slRz6cTAFh0a4MqpUDTl505iiasFmLHJtNdMJohCkz+KjjKG7fTU4ZHy5Sy2andeULbyD+31cT+ZQOgNR2F5aAHU3CYvfx0qFw9ph5PA1AWqz+FoClPolsOOZKwrkObanbQplJebavhAgMBAAECgYAreHtcWIMrRU4ydLOWXQXzb1jjUfZUpqx+qtjQbvmB07YJq+9IftWO9cWOeLGeNTTk1hS+PC1BJRiwk9X2pdEpdqlCbri8mKPlu+Z37ZB+sNRiyl+2p4sDx9WTvw8dJHIsWFlDNnbHzS0oDexlOxX68fL4NcsZu5VLQLZV0W5YAQJBAPyIvCj9gw0OT1LPcj4Yks5V+5pjr4g7NqFxKEfxtPJErE8Zjz6Zm8x0/k2E8XCd63lVk8Dh13TJSqfYwh/+ZkECQQDE2nHL/X3qN4EEqsWfbB8piAO7/5Ux956fCrhUYKXiIPJsHyiojePAw4nXlf1Nd+Fnu6rjG35xgSNmUbu7Wh2hAkBEKzj3q69jp9g712nUX1fJwSYhAAXTNYDCxcQE37djqqwE0jZ7xIVtBKvdCyUNrGNzJmmzKIO7r9aqRnXoowjBAkBi3Rqdyne8c5e2UlXiFRkpcIf/mQLDD4t4cJfWuJtXEBjwOE3hKTGjFBFcVpXanER2JohSevJr6uFud8oC8+VBAkEAgNXtGYF9xdffvrpsjmq5H54W2TWq1GPDm7oF0Ct9jduElxZx11cdtcWYAzdU+bYjr+jrbut4X3IqDFhUMCOYfw==";
    private final static String WX_URL = QFoodApi.BASE_URL + "Table/JsonSQL/weixinpay/prepay.php";
    private final static String ALI_URL = QFoodApi.BASE_URL + "Table/JsonSQL/alipay/notify_url.php";
    @Bind(R.id.tb_center_tv)
    TextView tbCenterTv;
    @Bind(R.id.tb_left_iv)
    ImageView tbLeftIv;
    @Bind(R.id.fl_tab)
    FrameLayout flTab;
    @Bind(R.id.tv_receiver)
    TextView tvReceiver;
    @Bind(R.id.tv_receiver_phone)
    TextView tvReceiverPhone;
    @Bind(R.id.tv_receiver_address)
    TextView tvReceiverAddress;
    @Bind(R.id.rcv_item_pay)
    RecyclerView rcvItemPay;
    @Bind(R.id.iv_wepay_check)
    ImageView ivWxpayCheck;
    @Bind(R.id.iv_alipay_check)
    ImageView ivAlipayCheck;

    ArrayList<PayModel.PayBean> payList;
    List<PayModel.PayBean> showList;
    List<String> payMerchantIdList = new ArrayList<>();
    NewOrderPresenter createOrderPresenter;
    String ipv4;
    String tradeNumber;
    int style = 0;//用来表示是否需要生成订单
    String receiver;
    String phone;
    String address;
    private PayItemAdapter payItemAdapter;


    @Override
    protected OrderInfo OnOrderCreate() {
        ipv4 = NetUtil.getPsdnIp();
        if (PayType.WeixinPay.equals(payType)) {
            return BuildOrderInfo("微信支付", "30m", WX_URL,
                    getOutTradeNo(),
                    getItemsName(payList),
                    getCheckItemTotalMoney(payList),
                    ipv4);
        }
        if (PayType.AliPay.equals(payType)) {
            return BuildOrderInfo("支付宝支付", "30m", ALI_URL,
                    getOutTradeNo(),
                    getItemsName(payList),
                    getCheckItemTotalMoney(payList),
                    ipv4);
        }
        return null;
    }

    private NewOrderBean generateOrder() {
        NewOrderBean _orderBean = new NewOrderBean();
        _orderBean.setReceiver(receiver);
        _orderBean.setPhone(phone);
        _orderBean.setAddress(address);
        _orderBean.setFood(getCheckedFood(payList));
        _orderBean.setClient(AccountUtil.getInstance().getUserID());
        _orderBean.setPrice(getCheckItemTotalMoney(payList));
        return _orderBean;
    }

    @Override
    public void dataReceive(Intent intent) {
        PayModel payModel = intent.getParcelableExtra(AppConstants.KEY_PARCELABLE);
        style = intent.getIntExtra(AppConstants.ORDER_STYLE, 0);
        if (payModel != null) {
            setAddress(payModel.getReceiver(), payModel.getPhone(), payModel.getAddress());
//            payMerchantIdList = getPayMerchantIdList(payList);
            payList = payModel.getPayBeanList();
            showList = getShowList(payList);
//            payList.addAll(payModel.getPayBeanList());
        }
    }

    private List<PayModel.PayBean> getShowList(ArrayList<PayModel.PayBean> payList) {
        List<PayModel.PayBean> _payBeenList = null;
        String last = "";
        for (PayModel.PayBean payBean :
                payList) {
            if (!last.equals(payBean.getMerchantId())) {
                if (_payBeenList == null)
                    _payBeenList = new ArrayList<>();
                _payBeenList.add(payBean);
            }
        }
        return _payBeenList;
    }

    private List<String> getPayMerchantIdList(ArrayList<PayModel.PayBean> payList) {
        /*Collections.sort(payList, new Comparator<PayModel.PayBean>() {
            @Override
            public int compare(PayModel.PayBean payBean1, PayModel.PayBean payBean2) {
                return payBean1.getMerchantId().compareTo(payBean2.getMerchantId());
            }
        });*/
        List<String> index = new ArrayList<>();
        String last = "";
        for (PayModel.PayBean payBean :
                payList) {
            if (!last.equals(payBean.getMerchantId())) {
                index.add(payBean.getMerchantId());
            }
        }
        return index;
    }

    @Override
    protected void initData() {
        tbCenterTv.setText(getResources().getString(R.string.tvPayTitle));
        flTab.setBackgroundColor(ContextCompat.getColor(this, R.color.colorBlueNormal));
        tbLeftIv.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_chevron_left_black));
        rcvItemPay.setLayoutManager(new LinearLayoutManager(this));
        rcvItemPay.setHasFixedSize(true);
        rcvItemPay.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager.HORIZONTAL,
                SizeUtil.dip2px(1), ContextCompat.getColor(this, R.color.colorInActive)));
        payItemAdapter = new PayItemAdapter(this, showList);
        payItemAdapter.setOnPayItemClick(this);
        rcvItemPay.setAdapter(payItemAdapter);

        RegisterBasePay(ALI_PARTNER, ALI_SELLER_ID, ALI_PRIVATE_KEY,
                InitApplication.WXID, WX_MERCHANT_ID, WX_PRIVATE_KEY);
        createOrder();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.pay_layout;
    }

    private void createOrder() {
        if (style == 0) {
            NewOrderBean newOrderBean = generateOrder();
            if (createOrderPresenter == null)
                createOrderPresenter = new NewOrderPresenter(this);
            createOrderPresenter.createNewOrder(newOrderBean);
        }
    }

    private void setAddress(String receiver, String phone, String address) {
        tvReceiver.setText(String.format(Locale.ENGLISH, getResources().getString(R.string.tvReceiver),
                receiver));
        tvReceiverPhone.setText(String.format(Locale.ENGLISH, getResources().getString(R.string.tvContactPhone),
                phone));
        tvReceiverAddress.setText(String.format(Locale.ENGLISH, getResources().getString(R.string.tvReceiveAddress),
                address));
    }

    /*支付成功的回调*/
    @Override
    protected void showPaySuccess(String s) {
        showPayResult(true);
    }

    private void showPayResult(boolean isSuccess) {
        ((ViewGroup) decorView).removeAllViews();
        LayoutInflater.from(this).inflate(isSuccess ? R.layout.pay_result_success_layout : R.layout.pay_result_error_layout, (ViewGroup) decorView);
    }

    @Override
    protected void showPayWarning(String s) {
        ToastHelper.getInstance()._toast(s);
    }

    @Override
    protected void showPayError(String s) {
        showPayResult(false);
    }

    @OnClick({R.id.tb_left_iv, R.id.iv_edit_right, R.id.bt_pay_affirmance,
            R.id.iv_wepay_check, R.id.iv_wepay, R.id.iv_alipay_check, R.id.iv_alipay})
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tb_left_iv:
                finish();
                break;
            case R.id.iv_edit_right:
                Intent _intent = new Intent(this, AddressActivity.class);
                _intent.setAction(AppConstants.ADDRESS_DEFAULT);
                startActivityForResult(_intent, 100);
                break;
            case R.id.bt_pay_affirmance:
                if (getCheckCount(payList) == 0) {
                    ToastHelper.getInstance().displayToastWithQuickClose("当前未选择商品！");
                    return;
                }
                ToastHelper.getInstance().displayToastWithQuickClose("准备支付");
                Pay(v);
                break;
            case R.id.iv_wepay_check:
            case R.id.iv_wepay:
                if (PayType.WeixinPay.equals(payType))
                    return;
                ivWxpayCheck.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_blue));
                if (PayType.AliPay.equals(payType)) {
                    ivAlipayCheck.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_uncheck_blue));
                    payType = PayType.WeixinPay;
                    break;
                }
                /*if (PayType.Cash.equals(payType)) {
                    ivCashCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck_green));
                    payType = PayType.WeixinPay;
                }*/
                break;
            case R.id.iv_alipay_check:
            case R.id.iv_alipay:
                if (PayType.AliPay.equals(payType))
                    return;
                ivAlipayCheck.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_check_blue));

                if (PayType.WeixinPay.equals(payType)) {
                    ivWxpayCheck.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_uncheck_blue));
                    payType = PayType.AliPay;
                    break;
                }
             /*   if (PayType.Cash.equals(payType)) {
                    ivCashCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck_green));
                    payType = PayType.AliPay;
                }*/
                break;
          /*  case R.id.iv_cash_check:
            case R.id.iv_cash:
                if (PayType.Cash.equals(payType))
                    return;
                ivCashCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_check_green));
                if (PayType.WeixinPay.equals(payType)) {
                    ivWxpayCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck_green));
                    payType = PayType.Cash;
                    break;
                }
                if (PayType.AliPay.equals(payType)) {
                    ivAlipayCheck.setImageDrawable(getResources().getDrawable(R.drawable.ic_uncheck_green));
                    payType = PayType.Cash;
                }
                break;*/
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (100 == resultCode) {

            if (data == null) {
                ToastHelper.getInstance().displayToastWithQuickClose("设置地址失败");
                return;
            }
            AddressUrlBean.AddressBean _addressBean = data.getParcelableExtra(AppConstants.ADDRESS_DEFAULT);
            if (_addressBean == null) {
                ToastHelper.getInstance().displayToastWithQuickClose("设置地址失败，请填写详细地址");
                return;
            }
            setAddress(_addressBean.getName(), _addressBean.getPhone(),
                    _addressBean.getAddress().concat(_addressBean.getDetail()));
        }
    }

    @Override
    public void onPayItemClick(int position) {
       /* if (payList.get(position).isChecked()) {
            payList.get(position).setChecked(false);
        } else
            payList.get(position).setChecked(true);
        payItemAdapter.setPayList(payList);*/
    }

    private int getCheckCount(List<PayModel.PayBean> payList) {
        int count = 0;
        for (PayModel.PayBean _payBean : payList) {
            if (_payBean.isChecked())
                count++;
        }
        return count;
    }

    private List<NewOrderBean.FoodBean> getCheckedFood(List<PayModel.PayBean> payList) {
        List<NewOrderBean.FoodBean> _bean = new ArrayList<>();
        for (PayModel.PayBean _payBean : payList) {
            if (_payBean.isChecked()) {
                NewOrderBean.FoodBean foodBean = new NewOrderBean.FoodBean();
                foodBean.setID(_payBean.getProductId());
                foodBean.setNum(_payBean.getProductNumber());
                _bean.add(foodBean);
            }
        }

        return _bean;
    }

    private String getCheckItemTotalMoney(List<PayModel.PayBean> payList) {
        String count = "0";
        for (PayModel.PayBean _payBean : payList) {
            if (_payBean.isChecked())
                count = DecimalUtil.addWithScale(count, _payBean.getProductPrice(), 2);
        }
        return count;
    }

    private String getItemsName(List<PayModel.PayBean> payList) {
        String concatName = "";
        for (PayModel.PayBean _payBean : payList) {
            concatName = concatName.concat(_payBean.getProductName());
        }
        return concatName;
    }

    @Override
    public void showData(Object o) {
        if (o instanceof NewOrderBackModel) {
            tradeNumber = ((NewOrderBackModel) o).getMsg();
//            ToastHelper.getInstance().displayToastWithQuickClose("正在支付中....");
            List<NewOrderBackModel.NewOrderBackBean> backBeanList = ((NewOrderBackModel) o).getRows();
            for (int i = 0; i < backBeanList.size(); ) {
                NewOrderBackModel.NewOrderBackBean _backList = backBeanList.get(i);
                int j = 0;
                for (; j < showList.size(); ) {
                    if (showList.get(j).getMerchantId().equals(_backList.getRId())) {
                        String fee = _backList.getFee();
                        String price = _backList.getPrice();
                        showList.get(j).setProductPrice(DecimalUtil.add(fee, price));
                        showList.get(j).setDeliverFee(fee);
                        i++;
                        break;
                    }
                }
                if (j == showList.size()) {
                    ToastHelper.getInstance().displayToastWithQuickClose("订单创建失败");
                    return;
                }
            }
           /* fee = ((NewOrderBackModel) o).getFee();
            price = ((NewOrderBackModel) o).getPrice();
//            Log.i("RHG", "Fee: " + fee + " price: " + price);
            payList.get(0).setProductPrice(DecimalUtil.add(fee, price));
            payList.get(0).setDeliverFee(fee);*/
            payItemAdapter.setPayList(showList);
        } else if (o instanceof String) {
            if (o.equals("new_order_error"))
                ToastHelper.getInstance()._toast("订单创建失败");
        }
    }

    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     */
    private String getOutTradeNo() {
        return style == 0 ? tradeNumber : payList.get(0).getProductId();//style=0时，productId为商品的ID；style=1时，productId为订单的ID。
    }
}
