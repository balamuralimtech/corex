$path='corex-web/src/main/resources/coreAppMessages_hi.properties'
$lines=Get-Content $path
$map=@{
'Currency'='मुद्रा'; 'Update'='अद्यतन'; 'Failed'='विफल'; 'Sub'='उप'; 'Modules'='मॉड्यूल'; 'Module'='मॉड्यूल'; 'Number'='संख्या';
'Of'='का'; 'Records'='रिकॉर्ड'; 'Designation'='पदनाम'; 'Removal'='हटाना'; 'Removed'='हटाया गया'; 'Successfully'='सफलतापूर्वक';
'State'='राज्य'; 'does'='है'; 'not'='नहीं'; 'exists'='मौजूद है'; 'City'='शहर'; 'already'='पहले से'; 'Engine'='इंजन';
'Subregion'='उपक्षेत्र'; 'SubRegion'='उपक्षेत्र'; 'Warning'='चेतावनी'; 'Warnings'='चेतावनियाँ'; 'Coverage'='कवरेज';
'Raw'='रॉ'; 'counts'='गणना'; 'support'='समर्थन'; 'derived'='व्युत्पन्न'; 'visuals'='दृश्य'; 'ISO'='ISO'; 'Code'='कोड';
'Website'='वेबसाइट'; 'Roles'='भूमिकाएँ'; 'Department'='विभाग'; 'Departments'='विभाग'; 'Signals'='संकेत'; 'deserve'='योग्य';
'escalate'='बढ़ें'; 'into'='में'; 'user-facing'='उपयोगकर्ता-सामने'; 'failures'='विफलताएँ'; 'Nationality'='राष्ट्रीयता';
'Exceptions'='अपवाद'; 'Exception'='अपवाद'; 'Flag'='ध्वज'; 'Longitude'='देशांतर'; 'Confirm'='पुष्टि करें';
'Reject'='अस्वीकार करें'; 'End'='समाप्ति'; 'Date'='तिथि'; 'Unhandled'='असंभाले'; 'framework-thrown'='फ्रेमवर्क-फेंके गए';
'traces'='ट्रेस'; 'caught'='पकड़े गए'; 'monitor'='मॉनिटर'; 'Monitored'='निगरानी किए गए'; 'Incidents'='घटनाएँ';
'Orgs'='संस्थाएँ'; 'smtpAuth'='SMTP प्रमाणीकरण'; 'Password'='पासवर्ड'; 'Currencies'='मुद्राएँ'; 'smtpHost'='SMTP होस्ट';
'Branch'='शाखा'; 'Branches'='शाखाएँ'; 'Session'='सत्र'; 'Approve'='स्वीकृत करें'; 'Username'='उपयोगकर्ता नाम'; 'Database'='डेटाबेस';
'Contact'='संपर्क'; 'smtpStarttlsEnable'='SMTP StartTLS सक्षम'; 'Error'='त्रुटि'; 'Product'='उत्पाद'; 'Capital'='राजधानी';
'View'='देखें'; 'Auto'='स्वतः'; 'Refresh'='रिफ्रेश'; 'Version'='संस्करण'; 'Geography'='भूगोल'; 'Footprint'='प्रभाव क्षेत्र';
'should'='चाहिए'; 'empty'='खाली'; 'Sign'='साइन'; 'In'='इन'; 'Start'='आरंभ'; 'Region'='क्षेत्र'; 'Countries'='देश';
'States'='राज्य'; 'Cities'='शहर'; 'Id'='आईडी'; 'Immediate'='तत्काल'; 'newest'='नवीनतम'; 'signal'='संकेत'; 'Structure'='संरचना';
'Inventory'='इन्वेंटरी'; 'Online'='ऑनलाइन'; 'Prepare'='तैयार करें'; 'Datasource'='डेटास्रोत'; 'Tracked'='ट्रैक की गई';
'Translations'='अनुवाद'; 'Connection'='कनेक्शन'; 'Stream'='स्ट्रीम'; 'reported'='रिपोर्ट किया गया'; 'by'='द्वारा'; 'metadata'='मेटाडेटा';
'Latitude'='अक्षांश'; 'Notification'='अधिसूचना'; 'Settings'='सेटिंग्स'; 'Seconds'='सेकंड'; 'Print'='प्रिंट'; 'unexpected'='अप्रत्याशित';
'Unexpected'='अप्रत्याशित'; 'Technical'='तकनीकी'; 'Details'='विवरण'; 'Info'='जानकारी'; 'Scanned'='स्कैन की गई'; 'Efficiency'='दक्षता';
'Radar'='रडार'; 'Latest'='नवीनतम'; 'included'='शामिल'; 'monitoring'='निगरानी'; 'sweep'='स्वीप'; 'inspect'='जाँचें'; 'review'='समीक्षा करें';
'instantly'='तुरंत'; 'artifact'='आर्टिफैक्ट'; 'without'='बिना'; 'leaving'='छोड़े'; 'Built'='बनाया गया'; 'fast'='तेज़'; 'triage'='जाँच';
'when'='जब'; 'production'='प्रोडक्शन'; 'UAT'='UAT'; 'starts'='शुरू होता है'; 'misbehaving'='समस्या करता है'; 'could'='सकता';
'resolved'='हल'; 'from'='से'; 'page'='पृष्ठ'; 'or'='या'; 'again'='फिर से'; 'load'='लोड'; 'data'='डेटा'; 'split'='विभाजन';
'Number'='संख्या'; 'Latest'='नवीनतम'; 'Clinic'='क्लिनिक'; 'Management'='प्रबंधन'; 'BankDetails'='बैंक विवरण'; 'designations'='पदनाम';
'Designations'='पदनाम'; 'Submodule'='उप मॉड्यूल'; 'Notification'='अधिसूचना'; 'Session'='सत्र'; 'Prepare'='तैयार करें'; 'license'='लाइसेंस';
'conditions'='शर्तें'; 'Terms'='नियम'; 'Conditions'='शर्तें'
}
foreach($k in @($map.Keys | Sort-Object Length -Descending)){
  $v = $map[$k]
  $pattern = '\b' + [regex]::Escape($k) + '\b'
  $lines = $lines | ForEach-Object { [regex]::Replace($_, $pattern, $v) }
}
Set-Content -Path $path -Value $lines -Encoding UTF8
