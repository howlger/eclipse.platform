/* -*-mode:java; c-basic-offset:2; -*- */
/**********************************************************************
Copyright (c) 2003, Atsuhiko Yamanaka, JCraft,Inc. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v10.html

Contributors:
    Atsuhiko Yamanaka, JCraft,Inc. - initial API and implementation.
**********************************************************************/
package org.eclipse.team.ccvs.ssh2;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.File;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;

public class CVSSSH2PreferencePage extends FieldEditorPreferencePage
  implements IWorkbenchPreferencePage {

  public static String KEY_PROXY="CVSSSH2PreferencePage.PROXY";
  public static String KEY_PROXY_TYPE="CVSSSH2PreferencePage.PROXY_TYPE";
  public static String KEY_PROXY_HOST="CVSSSH2PreferencePage.PROXY_HOST";
  public static String KEY_PROXY_PORT="CVSSSH2PreferencePage.PROXY_PORT";
  public static String KEY_PROXY_AUTH="CVSSSH2PreferencePage.PROXY_AUTH";
  public static String KEY_PROXY_USER="CVSSSH2PreferencePage.PROXY_USER";
  public static String KEY_PROXY_PASS="CVSSSH2PreferencePage.PROXY_PASS";
  public static String KEY_SSH2HOME="CVSSSH2PreferencePage.SSH2HOME";
  public static String KEY_KEYFILE="CVSSSH2PreferencePage.KEYFILE";
  
  // Temporary preference for using ssh2 instead of ssh1
  public static String KEY_USE_SSH2="CVSSSH2PreferencePage.SSH2_USE_SSH2";

  static String SOCKS5="SOCKS5";
  static String HTTP="HTTP";
  private static String HTTP_DEFAULT_PORT="80";
  private static String SOCKS5_DEFAULT_PORT="1080";

  static String DSA="DSA";
  static String RSA="RSA";

  private DirectoryFieldEditor ssh2homeEditor;

  private Label proxyTypeLabel;
  private Label proxyHostLabel;
  private Label proxyPortLabel;
  private Label proxyUserLabel;
  private Label proxyPassLabel;
  private Combo proxyTypeCombo;
  private Text proxyHostText;
  private Text proxyPortText;
  private Text proxyUserText;
  private Text proxyPassText;
  private Button enableProxy;
  private Button enableAuth;
  private Button enableSSH2;
  private boolean useProxy;
  private boolean useAuth;

  private Label keyTypeLabel;
  private Combo keyTypeCombo;
  private Button keyGenerate;
  private Button keyLoad;
  private Button saveKeyPair;
  //private FileFieldEditor keyFileName;
  private Label keyCommentLabel;
  private Text keyCommentText;
  private Label keyFingerPrintLabel;
  private Text keyFingerPrintText;
  private Label keyPassphrase1Label;
  private Text keyPassphrase1Text;
  private Label keyPassphrase2Label;
  private Text keyPassphrase2Text;
  private Label publicKeylabel;
  private Text publicKeyText;
  private KeyPair kpair=null;
  private String kpairComment;

  public CVSSSH2PreferencePage() {
    super(GRID);
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    setPreferenceStore(store);
    setDescription("General CVSSSH2 Settings:");
  }

  protected void createFieldEditors() {
  	enableSSH2=new Button(getFieldEditorParent(), SWT.CHECK);
  	enableSSH2.setText("Enable SSH2 instead of SSH1 (temporary until SSH2 is fully tested)");
  	GridData gd=new GridData();
  	gd.horizontalSpan=2;
  	enableSSH2.setLayoutData(gd);
  	
  	createSpacer(getFieldEditorParent(), 3);
  	
  	ssh2homeEditor=
      new DirectoryFieldEditor(KEY_SSH2HOME, 
			       "SSH2 Home", 
			       getFieldEditorParent());

    addField(ssh2homeEditor);

    createSpacer(getFieldEditorParent(), 3);

    createProxy(getFieldEditorParent(), 3);

    createSpacer(getFieldEditorParent(), 3);

    createKeyGeneration(getFieldEditorParent(), 3);
  }

  private void updateControls() {
    boolean enable=enableProxy.getSelection();
    proxyTypeLabel.setEnabled(enable);
    proxyTypeCombo.setEnabled(enable);
    proxyPortLabel.setEnabled(enable);
    proxyPortText.setEnabled(enable);
    proxyHostLabel.setEnabled(enable);
    proxyHostText.setEnabled(enable);

    enableAuth.setEnabled(enable);
    enable&=enableAuth.getSelection();
    proxyUserLabel.setEnabled(enable);
    proxyUserText.setEnabled(enable);
    proxyPassLabel.setEnabled(enable);
    proxyPassText.setEnabled(enable);

    enable=(kpair!=null);
    //keyFileName.setEnabled(enable);
    publicKeylabel.setEnabled(enable);
    publicKeyText.setEnabled(enable);
    keyFingerPrintLabel.setEnabled(enable);
    keyFingerPrintText.setEnabled(enable);
    keyCommentLabel.setEnabled(enable);
    keyCommentText.setEnabled(enable);
    keyPassphrase1Label.setEnabled(enable);
    keyPassphrase1Text.setEnabled(enable);
    keyPassphrase2Label.setEnabled(enable);
    keyPassphrase2Text.setEnabled(enable);
    saveKeyPair.setEnabled(enable);
  }

  public void init(IWorkbench workbench) {
  }

  protected void initialize() {
    super.initialize();
    initControls();
  }

  public static void initDefaults(IPreferenceStore store) {
    setDefault(store, KEY_SSH2HOME, JSchSession.default_ssh_home);
    setDefault(store, KEY_PROXY_TYPE, HTTP);
    setDefault(store, KEY_PROXY_PORT, HTTP_DEFAULT_PORT);
    setDefault(store, KEY_PROXY_AUTH, "false");
    setDefault(store, KEY_PROXY_USER, "");
    setDefault(store, KEY_PROXY_PASS, "");
    store.setDefault(KEY_USE_SSH2, false);
  }

  private static void setDefault(IPreferenceStore store, String key, String value){
    store.setDefault(key, value);
    if(store.getString(key).length()==0)
      store.setValue(key, value);
  }

  private void initControls(){
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    enableSSH2.setSelection(store.getBoolean(KEY_USE_SSH2));
    useProxy=store.getString(KEY_PROXY).equals("true");
    enableProxy.setSelection(useProxy);
    proxyHostText.setText(store.getString(KEY_PROXY_HOST));
    proxyPortText.setText(store.getString(KEY_PROXY_PORT));
    proxyTypeCombo.select(store.getString(KEY_PROXY_TYPE).equals(HTTP)?0:1);
    useAuth=store.getString(KEY_PROXY_AUTH).equals("true");
    enableAuth.setSelection(useAuth);
    proxyUserText.setText(store.getString(KEY_PROXY_USER));
    proxyPassText.setText(store.getString(KEY_PROXY_PASS));
    proxyPassText.setEchoChar('*');
    updateControls();
  }
  protected void createProxy(Composite composite, int columnSpan) {
    Group group=new Group(composite, SWT.NONE);
    group.setText("Proxy settings");
    GridLayout layout=new GridLayout();
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);
    group.setFont(composite.getFont());
    
    enableProxy=new Button(group, SWT.CHECK);
    enableProxy.setText("Enable proxy connection");
    gd=new GridData();
    gd.horizontalSpan=2;
    enableProxy.setLayoutData(gd);

    proxyTypeLabel=new Label(group, SWT.NONE);
    proxyTypeLabel.setText("Proxy type");
    proxyTypeCombo=new Combo(group, SWT.READ_ONLY);
    proxyTypeCombo.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyTypeCombo.setLayoutData(gd);
    proxyTypeCombo.addModifyListener(new ModifyListener () {
	public void modifyText(ModifyEvent e){
	  if(proxyPortText==null) return;
	  Combo combo=(Combo)(e.getSource());
	  String foo=combo.getText();
	  if(foo.equals(HTTP)){ 
	    proxyPortText.setText(HTTP_DEFAULT_PORT); 
	  }
	  else if(foo.equals(SOCKS5)){
	    proxyPortText.setText(SOCKS5_DEFAULT_PORT);
	  }
	} 
      });
    proxyTypeCombo.add(HTTP);
    proxyTypeCombo.add(SOCKS5);
    proxyTypeCombo.select(0);

    proxyHostLabel=new Label(group, SWT.NONE);
    proxyHostLabel.setText("Proxy host address");

    proxyHostText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyHostText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyHostText.setLayoutData(gd);

    proxyPortLabel=new Label(group, SWT.NONE);
    proxyPortLabel.setText("Proxy host port");

    proxyPortText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPortText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyPortText.setLayoutData(gd);

    enableAuth=new Button(group, SWT.CHECK);
    enableAuth.setText("Enable proxy authentication");
    gd=new GridData();
    gd.horizontalSpan=2;
    enableAuth.setLayoutData(gd);

    proxyUserLabel=new Label(group, SWT.NONE);
    proxyUserLabel.setText("Proxy user name");

    proxyUserText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyUserText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyUserText.setLayoutData(gd);

    proxyPassLabel=new Label(group, SWT.NONE);
    proxyPassLabel.setText("Proxy password");

    proxyPassText=new Text(group, SWT.SINGLE | SWT.BORDER);
    proxyPassText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    proxyPassText.setLayoutData(gd);


    //  performDefaults();

    enableProxy.addSelectionListener(new SelectionListener() {
	public void widgetSelected(SelectionEvent e) {
	  updateControls();
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
      });

    enableAuth.addSelectionListener(new SelectionListener() {
	public void widgetSelected(SelectionEvent e) {
	  updateControls();
	}
	public void widgetDefaultSelected(SelectionEvent e) {
	}
      });
  }

  protected void createKeyGeneration(Composite composite, int columnSpan) {
    Group group=new Group(composite, SWT.NONE);
    group.setText("Key Management");
    GridLayout layout=new GridLayout();
    layout.numColumns=2;
    group.setLayout(layout);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment=GridData.FILL;
    group.setLayoutData(gd);
    group.setFont(composite.getFont());
    
    keyTypeLabel=new Label(group, SWT.NONE);
    keyTypeLabel.setText("Key type");
    keyTypeCombo=new Combo(group, SWT.READ_ONLY);
    keyTypeCombo.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyTypeCombo.setLayoutData(gd);

    keyGenerate=new Button(group, SWT.NULL);
    keyGenerate.setText("Generate");
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyGenerate.setLayoutData(gd);

    keyLoad=new Button(group, SWT.NULL);
    keyLoad.setText("Load(not implemented)");
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    keyLoad.setLayoutData(gd);

    publicKeylabel=new Label(group, SWT.NONE);
    publicKeylabel.setText("You can paste this public key into the remote authorized_keys file:");
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    publicKeylabel.setLayoutData(gd);

    publicKeyText=new Text(group,SWT.MULTI|SWT.BORDER|SWT.V_SCROLL);
    publicKeyText.setText("");
    publicKeyText.setEditable(false);
    gd=new GridData();
    gd.horizontalSpan=columnSpan;
    gd.horizontalAlignment = GridData.FILL;
    gd.verticalAlignment = GridData.FILL;
    gd.grabExcessHorizontalSpace = true;
    gd.grabExcessVerticalSpace = true;
    publicKeyText.setLayoutData(gd);

    keyFingerPrintLabel=new Label(group, SWT.NONE);
    keyFingerPrintLabel.setText("Finger print");
    keyFingerPrintText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyFingerPrintText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyFingerPrintText.setLayoutData(gd);

    keyCommentLabel=new Label(group, SWT.NONE);
    keyCommentLabel.setText("Comment");
    keyCommentText=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyCommentText.setFont(group.getFont());
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyCommentText.setLayoutData(gd);

    keyPassphrase1Label=new Label(group, SWT.NONE);
    keyPassphrase1Label.setText("Passphrase");
    keyPassphrase1Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase1Text.setFont(group.getFont());
    keyPassphrase1Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyPassphrase1Text.setLayoutData(gd);

    keyPassphrase2Label=new Label(group, SWT.NONE);
    keyPassphrase2Label.setText("Confirm passphrase");
    keyPassphrase2Text=new Text(group, SWT.SINGLE | SWT.BORDER);
    keyPassphrase2Text.setFont(group.getFont());
    keyPassphrase2Text.setEchoChar('*');
    gd=new GridData(GridData.FILL_HORIZONTAL);
    keyPassphrase2Text.setLayoutData(gd);
    /*
    keyFileName=
      new FileFieldEditor(KEY_KEYFILE,
			  "File name:", 
			  true,
			  group);
    addField(keyFileName);
    */
    saveKeyPair=new Button(group, SWT.NULL);
    saveKeyPair.setText("Save");
    gd=new GridData(GridData.HORIZONTAL_ALIGN_END);
    gd.horizontalSpan=columnSpan;
    saveKeyPair.setLayoutData(gd);

    keyGenerate.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  JSch jsch=JSchSession.getJSch();
	  boolean ok=true;
	  String _type=keyTypeCombo.getText();

	  try{
	    int type=0;
	    if(_type.equals(DSA)){
	      type=KeyPair.DSA;
	    }
	    else if(_type.equals(RSA)){
	      type=KeyPair.RSA;
	    }
	    else{
	      return;
	    }

	    kpair=KeyPair.genKeyPair(jsch, type);
	    ByteArrayOutputStream out=new ByteArrayOutputStream();
	    kpairComment=_type+"-1024";
	    kpair.writePublicKey(out, kpairComment);
	    out.close();
	    publicKeyText.setText(out.toString());
	    keyFingerPrintText.setText(kpair.getFingerPrint());
	    keyCommentText.setText(kpairComment);
	    updateControls();
	  }
	  catch(IOException ee){
	    ok=false;
	  }
	  catch(JSchException ee){
	    ok=false;
	  }
	  MessageBox mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_INFORMATION);
	  mb.setMessage(_type+" 1024bits key is successfully generated.");
	  mb.open();
	}
      });

    saveKeyPair.addSelectionListener(new SelectionAdapter(){
	public void widgetSelected(SelectionEvent e){
	  if(kpair==null)return;

	  MessageBox mb;
	  String pass=keyPassphrase1Text.getText();
	  if(!pass.equals(keyPassphrase2Text.getText())){
	    mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_ERROR);
	    mb.setMessage("Given passphrases don't match.");
	    mb.open();
	    return;
	  }
	  if(pass.length()==0){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setMessage("Are you sure you want to save this private key without the passpharse protection?");
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  kpair.setPassphrase(pass);

	  IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
	  String home=ssh2homeEditor.getStringValue(); 
	  File _home=new File(home);

	  if(!_home.exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_QUESTION);
	    mb.setMessage(home+" does not exsit.\nAre you sure you want to create it?");
	    if(mb.open()==SWT.NO){
	      return;
	    }
	    if(!_home.mkdirs()){
	      return;
	    }
	  }

	  FileDialog fd=new FileDialog(getShell(), SWT.SAVE);
	  fd.setFilterPath(home);
	  String file=(kpair.getKeyType()==KeyPair.RSA) ? "id_rsa" : "id_dsa";
	  fd.setFileName(file);
	  file=fd.open();
	  if(file==null){ // cancel
	    return;
	  }

	  if(new File(file).exists()){
	    mb=new MessageBox(getShell(),SWT.YES|SWT.NO|SWT.ICON_WARNING);
	    mb.setMessage(file+" has already existed.\nAre you sure you want to over write it?");
	    if(mb.open()==SWT.NO){
	      return;
	    }
	  }

	  boolean ok=true;
	  try{
	    kpair.writePrivateKey(file);
	    kpair.writePublicKey(file+".pub", kpairComment);
	  }
	  catch(Exception ee){
	    ok=false;
	  }

	  if(ok){
	    mb=new MessageBox(getShell(),SWT.OK|SWT.ICON_INFORMATION);
	    mb.setMessage("Successfully saved."+"\n"+
			  "Private key: "+file+"\n"+
			  "Public key: "+file+".pub");
	    mb.open();
	  }
	}
      });

    keyTypeCombo.add(DSA);
    keyTypeCombo.add(RSA);
    keyTypeCombo.select(0);
  }

  public boolean performOk() {
    boolean result=super.performOk();
    if(result){
      IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
      store.setValue(KEY_PROXY, enableProxy.getSelection());
      store.setValue(KEY_PROXY_TYPE, proxyTypeCombo.getText());
      store.setValue(KEY_PROXY_HOST, proxyHostText.getText());
      store.setValue(KEY_PROXY_PORT, proxyPortText.getText());

      store.setValue(KEY_PROXY_AUTH, enableAuth.getSelection());
      store.setValue(KEY_PROXY_USER, proxyUserText.getText());
      store.setValue(KEY_PROXY_PASS, proxyPassText.getText());
      
      store.setValue(KEY_USE_SSH2, enableSSH2.getSelection());
    }
    CVSSSH2Plugin.getDefault().savePluginPreferences();
    return result;
  }

  public void performApply() {
    super.performApply();
    IPreferenceStore store=CVSSSH2Plugin.getDefault().getPreferenceStore();
    store.setValue(KEY_PROXY, enableProxy.getSelection());
    store.setValue(KEY_PROXY_TYPE, proxyTypeCombo.getText());
    store.setValue(KEY_PROXY_HOST, proxyHostText.getText());
    store.setValue(KEY_PROXY_PORT, proxyPortText.getText());

    store.setValue(KEY_PROXY_AUTH, enableAuth.getSelection());
    store.setValue(KEY_PROXY_USER, proxyUserText.getText());
    store.setValue(KEY_PROXY_PASS, proxyPassText.getText());
  }

  protected void performDefaults(){
    super.performDefaults();
    enableProxy.setSelection(false);
    proxyHostText.setText("");
    proxyPortText.setText(HTTP_DEFAULT_PORT);
    proxyTypeCombo.select(0);
    enableAuth.setSelection(false);
    proxyUserText.setText("");
    proxyPassText.setText("");
    updateControls();
  }

  protected void createSpacer(Composite composite, int columnSpan) {
    Label label=new Label(composite, SWT.NONE);
    GridData gd=new GridData();
    gd.horizontalSpan=columnSpan;
    label.setLayoutData(gd);
  }
}
