package com.bluebead38.opencvtesseractocr;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.provider.ContactsContract;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity {

    private Button mBtnCameraView;
    private Button mBtnPhoneContacts;
    private EditText mEditOcrResult;
    private TextView displayEmail;
    private TextView displayPhone;
    private TextView displayName;
    private TextView displayMPhone;
    private TextView displayWeb;
    private String datapath = "";
    private String lang = "";

    private int ACTIVITY_REQUEST_CODE = 1;

    static TessBaseAPI sTess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        // 뷰 선언
        mBtnCameraView = (Button) findViewById(R.id.btn_camera);
        mBtnPhoneContacts = (Button) findViewById(R.id.btn_to_phone);
        mEditOcrResult = (EditText) findViewById(R.id.edit_ocrresult);
        displayName = (TextView) findViewById(R.id.text_name);
        displayPhone = (TextView) findViewById(R.id.text_Hphone);
        displayEmail = (TextView) findViewById(R.id.text_email);
        displayMPhone = (TextView) findViewById(R.id.text_Mphone);
        displayWeb = (TextView) findViewById(R.id.text_web);
        sTess = new TessBaseAPI();


        // Tesseract 인식 언어를 한국어로 설정 및 초기화
        lang = "kor";
        datapath = getFilesDir()+ "/tesseract";

        if(checkFile(new File(datapath+"/tessdata")))
        {
            sTess.init(datapath, lang);
        }

        mBtnCameraView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mIttCamera = new Intent(MainActivity.this, com.bluebead38.opencvtesseractocr.CameraView.class);
                startActivityForResult(mIttCamera, ACTIVITY_REQUEST_CODE);
            }
        });

        mBtnPhoneContacts.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
                intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

                intent.putExtra(ContactsContract.Intents.Insert.NAME, displayName.getText());

                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, displayEmail.getText());
                intent .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);

                intent.putExtra(ContactsContract.Intents.Insert.PHONE, displayPhone.getText());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);

                startActivity(intent);
                //finish();
            }
        });
    }

    boolean checkFile(File dir)
    {
        //디렉토리가 없으면 디렉토리를 만들고 그 후에 파일을 카피
        if(!dir.exists() && dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath + "/tessdata/" + lang + ".traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
        return true;
    }

    void copyFiles()
    {
        AssetManager assetMgr = this.getAssets();

        InputStream is = null;
        OutputStream os = null;

        try {
            is = assetMgr.open("tessdata/"+lang+".traineddata");

            String destFile = datapath + "/tessdata/" + lang + ".traineddata";

            os = new FileOutputStream(destFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            is.close();
            os.flush();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void extractName(String str){
        final String NAME_REGEX = "(김|이|박|최|정|강|조|윤|장|임|한|오|서|신|권|황|안|송|전|홍|류|고|문|량|양|손|배|백|허|남|심|로|노|하|곽|성|차|주|우|구|민|유|나|진|지|엄|채|원|천|방|공|현|함|변|염|여|추|도|소|석|선|설|마|길|연|위|표|명|기|반|라|왕|금|인|옥|육|맹|제|모|남궁|탁|국|어|은|편|용|예|경|봉|사|부|황보|가|복|태|목|형|계|피|두|감|제갈|음|빈|동|온|사공|호|범|선우|좌|팽)+((\\s)?[가-힣]){1,3}(\\s)?$";
        Pattern p = Pattern.compile(NAME_REGEX, Pattern.MULTILINE);
        Matcher m =  p.matcher(str);
        if(m.find()){
            System.out.println(m.group());
            displayName.setText(m.group());
        }
    }

    public void extractEmail(String str) {
        final String EMAIL_REGEX = "([a-z0-9_\\.-]+)@([\\da-zA-Z\\.-]+)\\.([a-zA-z\\.]{2,6})";
        Pattern p = Pattern.compile(EMAIL_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);   // get a matcher object
        if(m.find()){
            System.out.println(m.group());
            displayEmail.setText(m.group());
        }
    }

    public void extractWebPage(String str) {
        System.out.println("Getting the webpage");
        final String EMAIL_REGEX = "[w|W]{3}\\.([\\da-zA-Z\\.-_]+)\\.([a-zA-Z\\.]{2,6})";
        Pattern p = Pattern.compile(EMAIL_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);   // get a matcher object
        if(m.find()){
            System.out.println(m.group());
            displayWeb.setText(m.group());
        }
    }

    public void extractHomeNum(String str) {
        System.out.println("Getting Home Number");
        final String HPHONE_REGEX = "0[2-9][0-9]?[)\\-. ]*(\\d{3,4})[\\-. ]*(\\d{4})";
        Pattern p = Pattern.compile(HPHONE_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);   // get a matcher object
        if (m.find()) {
            System.out.println(m.group());
            displayMPhone.setText(m.group());
        }
    }

    public void extractMobileNum(String str) {
        System.out.println("Getting Mobile Number");
        final String MPHONE_REGEX = "01[0|1|6|7|8|9][)\\-. ]*(\\d{3,4})[\\-. ]*(\\d{4})";
        Pattern p = Pattern.compile(MPHONE_REGEX, Pattern.MULTILINE);
        Matcher m = p.matcher(str);   // get a matcher object
        if (m.find()) {
            System.out.println(m.group());
            displayPhone.setText(m.group());
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String result = "";
        if(resultCode==RESULT_OK)
        {
            if(requestCode== ACTIVITY_REQUEST_CODE)
            {
                // 받아온 OCR 결과 출력
                result = data.getStringExtra("STRING_OCR_RESULT");

                ///////////////////////////////////////////////////////////////////수정한 부분에서 에러나는 것 없에면 잘 실행됨.
                /*result= result.replace("|", "l");
                result= result.replace("ⓒ", "@");   //일단 ⓒ만 수정
                String mail1="com";      //기본 com으로 설정. 추후에 추가
                StringBuffer sb = new StringBuffer(result);
                int idex = result.indexOf(mail1);
                if(result.charAt(idex-1)!='.')
                    sb.insert('.',idex);*/

                mEditOcrResult.setText(result);

                extractName(result);
                extractMobileNum(result);
                extractHomeNum(result);
                extractEmail(result);
                extractWebPage(result);
            }
        }
    }
}
