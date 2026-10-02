package com.flymaccin.thedaw;

import android.app.Activity;
import android.content.*;
import android.net.Uri;
import org.json.JSONObject;

public final class StudioMesh {
 public static final String ACTION="com.flymaccin.STUDIO_HANDOFF";
 public static final String MIME="application/vnd.flymaccin.studio+json";
 private StudioMesh(){}
 public static String receive(Activity a){
  Intent i=a.getIntent(); if(i==null)return null;
  String p=null;
  if(ACTION.equals(i.getAction())||Intent.ACTION_SEND.equals(i.getAction())) p=i.getStringExtra(Intent.EXTRA_TEXT);
  if(Intent.ACTION_VIEW.equals(i.getAction())&&i.getData()!=null) p=i.getData().getQueryParameter("payload");
  if(p!=null&&!p.trim().isEmpty()){a.getSharedPreferences("fme_mesh",0).edit().putString("last_bundle",p).apply();return p;}
  return null;
 }
 public static String last(Activity a){return a.getSharedPreferences("fme_mesh",0).getString("last_bundle","");}
 public static String bundle(String source,String workspace){
  try{return new JSONObject().put("mesh_version",1).put("source",source).put("workspace",workspace).put("timestamp",System.currentTimeMillis()).toString();}
  catch(Exception e){return "{\"mesh_version\":1}";}
 }
 public static void send(Activity a,String targetPackage,String payload){
  Intent i=new Intent(ACTION);i.setType(MIME);i.setPackage(targetPackage);i.putExtra(Intent.EXTRA_TEXT,payload);
  try{a.startActivity(i);}catch(Exception e){Toast.makeText(a,"Target app not installed",Toast.LENGTH_SHORT).show();}
 }
 public static void share(Activity a,String payload){
  Intent i=new Intent(Intent.ACTION_SEND);i.setType(MIME);i.putExtra(Intent.EXTRA_TEXT,payload);a.startActivity(Intent.createChooser(i,"Send through FME Studio Mesh"));
 }
}
