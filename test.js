const lib = require('./index.js')

const addresses = [
  '2MyEcnJgsHMdiHQrpaaknGDnqFohJo9dsMa',
  '2NBSh5iYxD3GjDsHURW5Yo5fyjwwgakPzNW',
  '2MtaE1ujrjsp7UxdCqjagMNA6HeVvM59kQh',
  '2NDvecZK61C7UxBxZZeRDMzQbB8hboG528L',
  '2N5mqcMVrwPxJuFb9gDejuBLEGjE7tYckA7',
  '2Mu2vbVnXPgWeNcRdWFWk1CWt3zXdFX4XdJ',
  '2N1dXA3ZyXZn8dxT2mEbcj4xid77Eu2Csbm',
  '2Mu1DvGh7LxJCvfSExrJxTDCPHRwANZBLwi',
  '2ND7aQkHTVaQpwbqjbXRrKbHa4pVuwga72f',
  '2NEEgvSDu5EZ5LZQ1idKpcZRztEH4UWC5f3',
  '2MxzoFLuQkYfkKXUCE6pz2uwVDfSVCxT8Lv',
  '2MxDKmtoorSt8NZpqiofHjTQYjyfqdH6CwA',
  '2MxnarmdvYQ8FEqCqevirsknXX3mSzEgA8P',
  '2N2WSiBJMCrpZhwYkD6GGukMnhfCNjQysHt',
  '2NDapFhpa1xNRXyz6CqRiWeK3YN3A95sw7E',
  '2MztSxfTzs1dPiY6QbFyNbuzrmEM6K98Dct',
  '2MyqdjWSijzGpcYhQoQnZ4hg7ak3575kzZa',
  '2N1k6tN4iJ1wjjZpzGQMfAHN9uygLPe3MUg',
  '2N91aZizuWXn4Qi5XMgvU1RCMEmSXPEerNR',
  '2N4a8tBbdLeTQkBcxt9pYuMd1DLA7L87Aby',
  '2N73h52ZnkNYYqayYNM2Fb6pQjJtdRoSaui',
  '2MyHRvTRHZsdpvMLwcbbu2Hmr6BAtFmfGSH',
  '2Mx9HkiLx1cdnvfzP8w1TkHbU8jRnDvtMrk',
  '2Mx3pt2g211fALHyZumETZbPQ6cp4CZVqvo',
  '2NC6SbyDDA2JcRRkGQyqpL66zvgjzyyfKmu',
  '2NE2YZ9Lo8MPxnYGLZNTej4BYfaUsoWVRwy',
  '2N52Qr6Z6XnkhwtJ8f7q3CDjc53zY6cDBiM',
  '2NEqy62pUn3PbADTiEKVuepPbnNGWM6vbhB',
  '2MurjE67sxiF751jKj1a5uFa8jp8ukgSUPQ',
  '2NCbZ4vJbKvDdjRXaoPZdXAb8y4FWCtxeGx',
  '2N6a2Zp7oYX1kz8aajnxC11WEMDUnPixvED',
  '2MuP3JJowEUJqPATaVZMwjD8qB2uBrKQRhc',
  '2NFj7zw2AQ52eVMzGRaxEpyJiWQXpA5FzfW',
  '2NA1n8ZDp81nbno75NXEpsRk4ud68Rvkudv',
  '2NCwfCc76Sf6xMYKCurghMHjnT3Mj9uEYyN',
  '2N43tpnpPm4E8YL2mM4dTvVYBmZwKckbi53',
  '2N62idRx46GsbSjCqdpRogGmyCJeYpSKLd3',
  '2NDoTfZtuxEmd8bHaAx2uKhfdEsfwS6f2Tk',
  '2NFAj3yaQTVKK5tXVVCHAww3Bv9BfGgD4gK',
  '2NFQ3vq37VkjGB5DHJahud8VHFWKVHLM1hU',
]

lib
  .getTransactions(addresses, { coin_type: 1 })
  .then(txs => {
    console.log('====then') // eslint-disable-line no-console
    console.log(txs) // eslint-disable-line no-console
  })
  .catch(err => {
    console.log('====catch') // eslint-disable-line no-console
    console.log(err) // eslint-disable-line no-console
  })
