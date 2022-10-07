//package com.abc.mirroring.ui.premium;
//
//import androidx.annotation.Nullable;
//import androidx.appcompat.app.AppCompatActivity;
//import android.content.SharedPreferences;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.Button;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.abc.mirroring.R;
//import com.android.billingclient.api.AcknowledgePurchaseParams;
//import com.android.billingclient.api.AcknowledgePurchaseResponseListener;
//import com.android.billingclient.api.BillingClient;
//import com.android.billingclient.api.BillingClientStateListener;
//import com.android.billingclient.api.BillingFlowParams;
//import com.android.billingclient.api.BillingResult;
//import com.android.billingclient.api.Purchase;
//import com.android.billingclient.api.PurchasesUpdatedListener;
//import com.android.billingclient.api.SkuDetails;
//import com.android.billingclient.api.SkuDetailsParams;
//import com.android.billingclient.api.SkuDetailsResponseListener;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import static com.android.billingclient.api.BillingClient.SkuType.SUBS;
//public class PurchaseActivity2 extends AppCompatActivity implements PurchasesUpdatedListener {
//  public static final String PREF_FILE= "MyPref";
//  public static final String SUBSCRIBE_KEY= "subscribe";
//  public static final String ITEM_SKU_SUBSCRIBE= "sub_premium";
//  TextView premiumContent,subscriptionStatus;
//  Button subscribe;
//  private BillingClient billingClient;
//  @Override
//  protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.purchase_activity);
//    premiumContent = (TextView) findViewById(R.id.premium_content);
//    subscriptionStatus=(TextView) findViewById(R.id.subscription_status);
//    subscribe=(Button) findViewById(R.id.subscribe);
//// Establish connection to billing client
////check subscription status from google play store cache
////to check if item is already Subscribed or subscription is not renewed and cancelled
//    billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
//    billingClient.startConnection(new BillingClientStateListener() {
//      @Override
//      public void onBillingSetupFinished(BillingResult billingResult) {
//        if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
//          Purchase.PurchasesResult queryPurchase = billingClient.queryPurchases(SUBS);
//          List<Purchase> queryPurchases = queryPurchase.getPurchasesList();
//          if(queryPurchases!=null && queryPurchases.size()>0){
//            handlePurchases(queryPurchases);
//          }
////if no item in purchase list means subscription is not subscribed
////Or subscription is cancelled and not renewed for next month
//// so update pref in both cases
//// so next time on app launch our premium content will be locked again
//          else{
//            saveSubscribeValueToPref(false);
//          }
//        }
//      }
//      @Override
//      public void onBillingServiceDisconnected() {
//        Toast.makeText(getApplicationContext(),"Service Disconnected",Toast.LENGTH_SHORT).show();
//      }
//    });
////item subscribed
//    if(getSubscribeValueFromPref()){
//      subscribe.setVisibility(View.GONE);
//      premiumContent.setVisibility(View.VISIBLE);
//      subscriptionStatus.setText("Subscription Status : Subscribed");
//    }
////item not subscribed
//    else{
//      premiumContent.setVisibility(View.GONE);
//      subscribe.setVisibility(View.VISIBLE);
//      subscriptionStatus.setText("Subscription Status : Not Subscribed");
//    }
//  }
//  private SharedPreferences getPreferenceObject() {
//    return getApplicationContext().getSharedPreferences(PREF_FILE, 0);
//  }
//  private SharedPreferences.Editor getPreferenceEditObject() {
//    SharedPreferences pref = getApplicationContext().getSharedPreferences(PREF_FILE, 0);
//    return pref.edit();
//  }
//  private boolean getSubscribeValueFromPref(){
//    return getPreferenceObject().getBoolean( SUBSCRIBE_KEY,false);
//  }
//  private void saveSubscribeValueToPref(boolean value){
//    getPreferenceEditObject().putBoolean(SUBSCRIBE_KEY,value).commit();
//  }
//  //initiate purchase on button click
//  public void subscribe(View view) {
////check if service is already connected
//    if (billingClient.isReady()) {
//      initiatePurchase();
//    }
////else reconnect service
//    else{
//      billingClient = BillingClient.newBuilder(this).enablePendingPurchases().setListener(this).build();
//      billingClient.startConnection(new BillingClientStateListener() {
//        @Override
//        public void onBillingSetupFinished(BillingResult billingResult) {
//          if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//            initiatePurchase();
//          } else {
//            Toast.makeText(getApplicationContext(),"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
//          }
//        }
//        @Override
//        public void onBillingServiceDisconnected() {
//          Toast.makeText(getApplicationContext(),"Service Disconnected ",Toast.LENGTH_SHORT).show();
//        }
//      });
//    }
//  }
//  private void initiatePurchase() {
//    List<String> skuList = new ArrayList<>();
//    skuList.add(ITEM_SKU_SUBSCRIBE);
//    SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
//    params.setSkusList(skuList).setType(SUBS);
//    BillingResult billingResult = billingClient.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS);
//    if(billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//      billingClient.querySkuDetailsAsync(params.build(),
//        new SkuDetailsResponseListener() {
//          @Override
//          public void onSkuDetailsResponse(BillingResult billingResult,
//                                           List<SkuDetails> skuDetailsList) {
//            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
//              if (skuDetailsList != null && skuDetailsList.size() > 0) {
//                BillingFlowParams flowParams = BillingFlowParams.newBuilder()
//                  .setSkuDetails(skuDetailsList.get(0))
//                  .build();
//                billingClient.launchBillingFlow(PurchaseActivity2.this, flowParams);
//
//              } else {
////try to add subscription item "sub_example" in google play console
//                Toast.makeText(getApplicationContext(), "Item not Found", Toast.LENGTH_SHORT).show();
//              }
//            } else {
//              Toast.makeText(getApplicationContext(),
//                " Error " + billingResult.getDebugMessage(), Toast.LENGTH_SHORT).show();
//            }
//          }
//        });
//    }else{
//      Toast.makeText(getApplicationContext(),
//        "Sorry Subscription not Supported. Please Update Play Store", Toast.LENGTH_SHORT).show();
//    }
//  }
//  @Override
//  public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
////if item subscribed
//    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
//      handlePurchases(purchases);
//    }
////if item already subscribed then check and reflect changes
//    else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
//      Purchase.PurchasesResult queryAlreadyPurchasesResult = billingClient.queryPurchases(SUBS);
//      List<Purchase> alreadyPurchases = queryAlreadyPurchasesResult.getPurchasesList();
//      if(alreadyPurchases!=null){
//        handlePurchases(alreadyPurchases);
//      }
//    }
////if Purchase canceled
//    else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
//      Toast.makeText(getApplicationContext(),"Purchase Canceled",Toast.LENGTH_SHORT).show();
//    }
//// Handle any other error msgs
//    else {
//      Toast.makeText(getApplicationContext(),"Error "+billingResult.getDebugMessage(),Toast.LENGTH_SHORT).show();
//    }
//  }
//  void handlePurchases(List<Purchase>  purchases) {
//    for(Purchase purchase:purchases) {
////if item is purchased
//      if (ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus()) && purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED)
//      {
//        if (!verifyValidSignature(purchase.getOriginalJson(), purchase.getSignature())) {
//// Invalid purchase
//// show error to user
//          Toast.makeText(getApplicationContext(), "Error : invalid Purchase", Toast.LENGTH_SHORT).show();
//          return;
//        }
//// else purchase is valid
////if item is purchased and not acknowledged
//        if (!purchase.isAcknowledged()) {
//          AcknowledgePurchaseParams acknowledgePurchaseParams =
//            AcknowledgePurchaseParams.newBuilder()
//              .setPurchaseToken(purchase.getPurchaseToken())
//              .build();
//          billingClient.acknowledgePurchase(acknowledgePurchaseParams, ackPurchase);
//        }
////else item is purchased and also acknowledged
//        else {
//// Grant entitlement to the user on item purchase
//// restart activity
//          if(!getSubscribeValueFromPref()){
//            saveSubscribeValueToPref(true);
//            Toast.makeText(getApplicationContext(), "Item Purchased", Toast.LENGTH_SHORT).show();
//            this.recreate();
//          }
//        }
//      }
////if purchase is pending
//      else if( ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus()) && purchase.getPurchaseState() == Purchase.PurchaseState.PENDING)
//      {
//        Toast.makeText(getApplicationContext(),
//          "Purchase is Pending. Please complete Transaction", Toast.LENGTH_SHORT).show();
//      }
////if purchase is unknown mark false
//      else if(ITEM_SKU_SUBSCRIBE.equals(purchase.getSkus()) && purchase.getPurchaseState() == Purchase.PurchaseState.UNSPECIFIED_STATE)
//      {
//        saveSubscribeValueToPref(false);
//        premiumContent.setVisibility(View.GONE);
//        subscribe.setVisibility(View.VISIBLE);
//        subscriptionStatus.setText("Subscription Status : Not Subscribed");
//        Toast.makeText(getApplicationContext(), "Purchase Status Unknown", Toast.LENGTH_SHORT).show();
//      }
//    }
//  }
//  AcknowledgePurchaseResponseListener ackPurchase = new AcknowledgePurchaseResponseListener() {
//    @Override
//    public void onAcknowledgePurchaseResponse(BillingResult billingResult) {
//      if(billingResult.getResponseCode()==BillingClient.BillingResponseCode.OK){
////if purchase is acknowledged
//// Grant entitlement to the user. and restart activity
//        saveSubscribeValueToPref(true);
//        PurchaseActivity2.this.recreate();
//      }
//    }
//  };
//  /**
//   * Verifies that the purchase was signed correctly for this developer's public key.
//   * <p>Note: It's strongly recommended to perform such check on your backend since hackers can
//   * replace this method with "constant true" if they decompile/rebuild your app.
//   * </p>
//   */
//  private boolean verifyValidSignature(String signedData, String signature) {
//    try {
//// To get key go to Developer Console > Select your app > Development Tools > Services & APIs.
//      String base64Key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhaD7/LERI6BJO8qTUFCi2P0B7cqwtygVUGiIsn7Rvxt8XHdINP57pJF9Zc/VXaH8E7oLJS5oJUZlkoVngrg9sKBWQDxcPiz2lj6I5a2ImXOgBl4in7MHFcLwSbNjdcxXWr5Pj86t4qsGOTs6BF7oZpa11JgY0Kp5VCLHjCJLWyiz/HRTR7yceJIO1xEUsH7WTeeZYjKc0JLtDSENQRtHDk38q93T3JHKXeZgKaX2J4STcnibVg0KpAg1tRJl3QcJXsFOh9WlGyRYB2zpnXGVjMS/3PoBEkoMpDzhXW7la57AdRLPonPQPkhZpeh1RTWmiSp3Rk8jXUeQEsw/zwuMfQIDAQAB";
//      return Security.verifyPurchase(base64Key, signedData, signature);
//    } catch (IOException e) {
//      return false;
//    }
//  }
//  @Override
//  protected void onDestroy() {
//    super.onDestroy();
//    if(billingClient!=null){
//      billingClient.endConnection();
//    }
//  }
//}
