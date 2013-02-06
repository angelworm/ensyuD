#/bin/sh

if test $# -lt 1
then
    dest="."
else
    dest=$1
fi

if test ! -e ${dest}
then
    mkdir ${dest}
fi


echo '#!'`which python` >> tmp
cat pycasl2.py >> tmp
mv tmp ${dest}/pycasl2
chmod +x ${dest}/pycasl2

echo '#!'`which python` >> tmp
cat pycomet2.py >> tmp
mv tmp ${dest}/pycomet2
chmod +x ${dest}/pycomet2
