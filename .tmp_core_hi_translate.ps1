$engPath = 'corex-web/src/main/resources/coreAppMessages.properties'
$hiPath = 'corex-web/src/main/resources/coreAppMessages_hi.properties'

$engLines = Get-Content $engPath
$currentHi = @{}
foreach($line in Get-Content $hiPath){
  if($line -match '^(.*?)=(.*)$'){
    $currentHi[$matches[1]] = $matches[2]
  }
}

$phraseMap = [ordered]@{
  'Organization already exists'='संस्था पहले से मौजूद है'
  'Active Licenses'='सक्रिय लाइसेंस'
  'Expired Licenses'='समाप्त लाइसेंस'
  'Expiring Soon'='जल्द समाप्त होने वाले'
  'Expiring in 30 Days'='30 दिनों में समाप्त होने वाले'
  'License Command Center'='लाइसेंस कमांड सेंटर'
  'License Health Score'='लाइसेंस स्वास्थ्य स्कोर'
  'Renewal Pressure Board'='नवीनीकरण दबाव बोर्ड'
  'Exposure Summary'='एक्सपोज़र सारांश'
  'Command Notes'='कमांड नोट्स'
  'Application Admin'='एप्लिकेशन एडमिन'
  'All Organizations'='सभी संस्थाएँ'
  'All Users'='सभी उपयोगकर्ता'
  'User Administration Command Center'='उपयोगकर्ता प्रशासन कमांड सेंटर'
  'User Administration'='उपयोगकर्ता प्रशासन'
  'User Management'='उपयोगकर्ता प्रबंधन'
  'Users Grid'='उपयोगकर्ता ग्रिड'
  'User Scope'='उपयोगकर्ता दायरा'
  'Active Users'='सक्रिय उपयोगकर्ता'
  'Activity Controls'='गतिविधि नियंत्रण'
  'Activity Distribution'='गतिविधि वितरण'
  'Activity Timeline'='गतिविधि समयरेखा'
  'Activity Mix'='गतिविधि मिश्रण'
  'User Activity Intelligence'='उपयोगकर्ता गतिविधि अंतर्दृष्टि'
  'User Activity'='उपयोगकर्ता गतिविधि'
  'Security Events'='सुरक्षा घटनाएँ'
  'Login Events'='लॉगिन घटनाएँ'
  'Logout Events'='लॉगआउट घटनाएँ'
  'Change Events'='परिवर्तन घटनाएँ'
  'Change Operations'='परिवर्तन संचालन'
  'Browser Type Distribution'='ब्राउज़र प्रकार वितरण'
  'Browser Mix'='ब्राउज़र मिश्रण'
  'Audit Grid'='ऑडिट ग्रिड'
  'Status Comparison'='स्थिति तुलना'
  'Status Share'='स्थिति अनुपात'
  'Top Activity Type'='शीर्ष गतिविधि प्रकार'
  'Overall User Activity Status'='कुल उपयोगकर्ता गतिविधि स्थिति'
  'Most Active User'='सबसे सक्रिय उपयोगकर्ता'
  'Profile Image'='प्रोफ़ाइल चित्र'
  'Profile details are not available'='प्रोफ़ाइल विवरण उपलब्ध नहीं हैं'
  'Current saved profile picture for this user account.'='इस उपयोगकर्ता खाते के लिए वर्तमान सहेजी गई प्रोफ़ाइल तस्वीर।'
  'No user activity found'='कोई उपयोगकर्ता गतिविधि नहीं मिली'
  'Home'='होम'
  'Dashboard'='डैशबोर्ड'
  'System'='सिस्टम'
  'Window'='विंडो'
  'Expiry'='समाप्ति'
  'Health'='स्वास्थ्य'
  'Healthy'='स्वस्थ'
  'Image'='चित्र'
  'Search Users'='उपयोगकर्ता खोजें'
  'Fetch Users'='उपयोगकर्ता प्राप्त करें'
  'Selected File'='चयनित फ़ाइल'
  'Selected Size'='चयनित आकार'
  'Download Selected'='चयनित डाउनलोड करें'
  'Download ZIP'='ZIP डाउनलोड करें'
  'Open Full Viewer'='पूर्ण व्यूअर खोलें'
  'Refresh Explorer'='एक्सप्लोरर रिफ्रेश करें'
  'Notify'='सूचित करें'
  'Remarks'='टिप्पणियाँ'
  'Search'='खोजें'
  'Unknown'='अज्ञात'
  'Created at'='निर्मित समय'
  'Updated At'='अद्यतन समय'
  'Last Seen'='अंतिम बार देखा गया'
  'Last Recorded'='अंतिम रिकॉर्ड'
  'Last Logout'='अंतिम लॉगआउट'
  'Last Password Change'='अंतिम पासवर्ड परिवर्तन'
  'Last Successful Login'='अंतिम सफल लॉगिन'
  'Phone'='फ़ोन'
  'Email address'='ईमेल पता'
  'Message'='संदेश'
  'Status'='स्थिति'
  'Actions'='क्रियाएँ'
  'Add'='जोड़ें'
  'Delete'='हटाएँ'
  'files'='फ़ाइलें'
  'lines'='पंक्तियाँ'
  'events'='घटनाएँ'
  'Live Preview'='लाइव पूर्वावलोकन'
  'Live'='लाइव'
  'New User'='नया उपयोगकर्ता'
  'No file selected'='कोई फ़ाइल चयनित नहीं'
  'Never Logged In'='कभी लॉगिन नहीं किया'
  'Server Log Command Deck'='सर्वर लॉग कमांड डेक'
  'Server Logs'='सर्वर लॉग'
  'Server and DB'='सर्वर और डेटाबेस'
  'Log Explorer'='लॉग एक्सप्लोरर'
  'Log File'='लॉग फ़ाइल'
  'Log Files'='लॉग फ़ाइलें'
  'Log Location'='लॉग स्थान'
  'Application Notifications'='एप्लिकेशन अधिसूचनाएँ'
  'Application Notification Details'='एप्लिकेशन अधिसूचना विवरण'
  'Bank Details'='बैंक विवरण'
  'Bank Account Details*'='बैंक खाता विवरण*'
  'Created By'='निर्माता'
  'Organization name'='संस्था का नाम'
  'State*'='राज्य*'
  'City*'='शहर*'
  'Country*'='देश*'
  'Branch*'='शाखा*'
  'Time'='समय'
  'Timeline'='समयरेखा'
  'Total Records'='कुल रिकॉर्ड'
  'Total visible records'='कुल दृश्य रिकॉर्ड'
  'Unique IPs'='अद्वितीय IP'
  'Activities per role'='प्रति भूमिका गतिविधियाँ'
  'activities per user'='प्रति उपयोगकर्ता गतिविधियाँ'
  'Active User Rate'='सक्रिय उपयोगकर्ता दर'
  'Active / expired licenses'='सक्रिय / समाप्त लाइसेंस'
  'Awaiting selection'='चयन की प्रतीक्षा'
  'Closest expiry'='सबसे निकट समाप्ति'
  'Earliest Expiry'='सबसे जल्दी समाप्ति'
}

$wordMap = @{
  'license'='लाइसेंस'; 'licenses'='लाइसेंस'; 'coverage'='कवरेज'; 'currently'='वर्तमान में'; 'available'='उपलब्ध';
  'accessible'='सुलभ'; 'organizations'='संस्थाएँ'; 'organization'='संस्था'; 'users'='उपयोगकर्ता'; 'user'='उपयोगकर्ता';
  'accounts'='खाते'; 'account'='खाता'; 'lifecycle'='जीवनचक्र'; 'status'='स्थिति'; 'for'='के लिए'; 'and'='और';
  'controls'='नियंत्रण'; 'control'='नियंत्रण'; 'summary'='सारांश'; 'details'='विवरण'; 'search'='खोजें'; 'fetch'='प्राप्त करें';
  'export'='निर्यात'; 'maintain'='बनाए रखें'; 'profile'='प्रोफ़ाइल'; 'preview'='पूर्वावलोकन'; 'selected'='चयनित';
  'log'='लॉग'; 'file'='फ़ाइल'; 'files'='फ़ाइलें'; 'open'='खोलें'; 'current'='वर्तमान'; 'download'='डाउनलोड';
  'server'='सर्वर'; 'compare'='तुलना करें'; 'active'='सक्रिय'; 'inactive'='निष्क्रिय'; 'unknown'='अज्ञात';
  'create'='बनाएँ'; 'manage'='प्रबंधित करें'; 'scope'='दायरा'; 'window'='विंडो'; 'activity'='गतिविधि';
  'distribution'='वितरण'; 'timeline'='समयरेखा'; 'mix'='मिश्रण'; 'browser'='ब्राउज़र'; 'type'='प्रकार';
  'name'='नाम'; 'health'='स्वास्थ्य'; 'image'='चित्र'; 'home'='होम'; 'dashboard'='डैशबोर्ड'; 'system'='सिस्टम';
  'email'='ईमेल'; 'address'='पता'; 'created'='निर्मित'; 'updated'='अद्यतन'; 'last'='अंतिम'; 'login'='लॉगिन';
  'logout'='लॉगआउट'; 'security'='सुरक्षा'; 'bank'='बैंक'; 'rate'='दर'; 'per'='प्रति'; 'role'='भूमिका';
  'all'='सभी'; 'admin'='एडमिन'; 'application'='एप्लिकेशन'; 'command'='कमांड'; 'notes'='नोट्स'; 'exposure'='एक्सपोज़र';
  'renewal'='नवीनीकरण'; 'pressure'='दबाव'; 'board'='बोर्ड'; 'closest'='निकटतम'; 'earliest'='सबसे जल्दी';
  'expiring'='समाप्त होने वाले'; 'expired'='समाप्त'; 'healthy'='स्वस्थ'; 'live'='लाइव'; 'new'='नया';
  'never'='कभी नहीं'; 'most'='सबसे अधिक'; 'exposed'='एक्सपोज़्ड'; 'loaded'='लोड किए गए'; 'visible'='दृश्य';
  'total'='कुल'; 'intelligence'='अंतर्दृष्टि'; 'overall'='कुल'; 'top'='शीर्ष'; 'share'='अनुपात'; 'comparison'='तुलना';
  'grid'='ग्रिड'; 'dialog'='संवाद'; 'directory'='निर्देशिका'; 'folders'='फ़ोल्डर'; 'inline'='इनलाइन';
  'full'='पूर्ण'; 'viewer'='व्यूअर'; 'size'='आकार'; 'pdf'='PDF'; 'zip'='ZIP'; 'ip'='IP'
}

function Has-Devanagari([string]$text){
  return $text -match '[\u0900-\u097F]'
}

function Auto-Translate([string]$value){
  $result = $value
  foreach($entry in $phraseMap.GetEnumerator() | Sort-Object { $_.Key.Length } -Descending){
    $result = $result.Replace($entry.Key, $entry.Value)
  }
  $result = [regex]::Replace($result, '[A-Za-z][A-Za-z\-/]*', {
    param($m)
    $token = $m.Value
    $low = $token.ToLowerInvariant()
    if($wordMap.ContainsKey($low)) { return $wordMap[$low] }
    return $token
  })
  return ([regex]::Replace($result, '\s+', ' ')).Trim()
}

$out = New-Object System.Collections.Generic.List[string]
foreach($line in $engLines){
  if($line -notmatch '^(.*?)=(.*)$'){
    $out.Add($line)
    continue
  }
  $key = $matches[1]
  $engValue = $matches[2]
  $existing = if($currentHi.ContainsKey($key)) { $currentHi[$key] } else { '' }
  if($key -eq 'applicationNotificationsFileLabel'){
    $out.Add($key + '=application-notifications')
  } elseif(Has-Devanagari $existing) {
    $out.Add($key + '=' + $existing)
  } else {
    $out.Add($key + '=' + (Auto-Translate $engValue))
  }
}
Set-Content -Path $hiPath -Value $out -Encoding UTF8
