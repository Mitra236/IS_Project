$(":file").change(function(event) {
    var files = this.files;
    for (var i = 0; i < files.length; i++) {
      (function(n) {
        var img = new Image;
        img.onload = function() {
          $("body").append(this)
        }
        img.src = window.URL.createObjectURL(files[n])
      }(i))
    }
  })