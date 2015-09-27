
procedure java.lang.Object$java.lang.Object$clone$43($this:ref) 
	returns ($other:ref);
	ensures ($other!=$null);
	ensures ($heap[$other,$alloc]==true);
	ensures ($heap[$other,$type] == $heap[$this,$type] );

procedure int$java.lang.String$compareTo$87($this:ref, $other:ref) 
	returns ($return:int); 
