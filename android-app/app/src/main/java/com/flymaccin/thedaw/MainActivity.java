package com.flymaccin.thedaw;

import android.app.*;import android.os.*;import android.graphics.Color;import android.view.*;import android.widget.*;

public class MainActivity extends Activity{
 int gold=Color.rgb(214,179,90),panel=Color.rgb(24,24,31),white=Color.rgb(240,240,244),muted=Color.rgb(145,145,155),green=Color.rgb(80,200,130);
 String workspace="HOME";
 public void onCreate(Bundle b){super.onCreate(b);getWindow().setStatusBarColor(Color.BLACK);String incoming=StudioMesh.receive(this);showHome();if(incoming!=null)Toast.makeText(this,"Studio Mesh project received",Toast.LENGTH_SHORT).show();}
 protected void onNewIntent(android.content.Intent i){super.onNewIntent(i);setIntent(i);String incoming=StudioMesh.receive(this);if(incoming!=null){showHome();Toast.makeText(this,"Studio Mesh project received",Toast.LENGTH_SHORT).show();}}
 TextView t(String s,int z,int c){TextView v=new TextView(this);v.setText(s);v.setTextSize(z);v.setTextColor(c);v.setPadding(18,12,18,12);return v;}
 Button b(String s){Button x=new Button(this);x.setText(s);x.setTextColor(white);x.setBackgroundColor(panel);return x;}
 void addRoute(LinearLayout p,String title,String pkg){Button x=b(title);x.setOnClickListener(v->StudioMesh.send(this,pkg,StudioMesh.bundle("theDAW",workspace)));p.addView(x);}
 void showHome(){ScrollView sc=new ScrollView(this);LinearLayout p=new LinearLayout(this);p.setOrientation(LinearLayout.VERTICAL);p.setPadding(24,18,24,18);p.setBackgroundColor(Color.rgb(9,9,12));
  p.addView(t("theDAW Android",28,gold));p.addView(t("FME STUDIO MESH · STANDALONE APK",12,muted));
  for(String s:new String[]{"MAKE","EDIT","MIX","SCORE","SING","DJ","VJ"}){Button x=b(s);x.setOnClickListener(v->{workspace=s;Toast.makeText(this,s+" workspace",Toast.LENGTH_SHORT).show();});p.addView(x);}
  p.addView(t("CONNECTED STUDIOS",14,green));addRoute(p,"SEND TO DEMONIC","com.flymaccin.demonicaistudio");addRoute(p,"SEND TO GENERIC DAW","com.flymaccin.genericdaw");addRoute(p,"SEND TO MEADOWLARK","com.flymaccin.meadowlarkdemonic");addRoute(p,"SEND TO TUNEFLOW","com.flymaccin.tuneflow");addRoute(p,"SEND TO MAC-MAESTRO","com.flymaccin.macmaestro");
  Button share=b("SHARE TO ANY STUDIO");share.setOnClickListener(v->StudioMesh.share(this,StudioMesh.bundle("theDAW",workspace)));p.addView(share);
  String last=StudioMesh.last(this);p.addView(t(last.isEmpty()?"No incoming mesh project yet.":"Incoming mesh project cached and available to this APK.",12,muted));
  sc.addView(p);setContentView(sc);
 }
}
