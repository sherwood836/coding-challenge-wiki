<!DOCTYPE html>
<html>
<head>
  <#include "header.ftl">
</head>

<body>

  <#include "nav.ftl">

<div class="container">
<form action="/findPath" method="GET">
Wikipedia starting URL:  <input type="text" name="start"/>

<input type="submit"/>

</div>

</body>
</html>
