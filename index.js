const axios = require('axios')
const util = require('util')

let binding = null
function loadBinding() {
  if (!binding) {
    binding = require('bindings')('ledgerapp_nodejs')
  }
}

loadBinding()

// -----------------------------------------------------------------------------
//                           NODEJS IMPLEMENTATIONS
// -----------------------------------------------------------------------------

const NJSHttpImpl = {}

NJSHttpImpl.get = (url, headers, callback) => {
  const header = {
    method: 'get',
    url: url,
  }

  if (headers.length > 0) {
    const tokenHeader = {}
    headers.map(header => {
      tokenHeader[header.field] = header.value
    })
    header.headers = tokenHeader
  }

  axios(header)
    .then(response => callback.on_success(response.status, JSON.stringify(response.data)))
    .catch(err => callback.on_network_error(err))
}

/*
    NJSItfExecutionContext Implementation
 */
const NJSContextImpl = {}

NJSContextImpl.execute = runnable => runnable.run()

/*
    NJSItfThreadDispatcher Implementation
 */
const NJSThreadDispatcherImpl = {
  contexts: {},
}

NJSThreadDispatcherImpl.getSerialExecutionContext = name => {
  let currentContext = NJSThreadDispatcherImpl.contexts[name]
  if (currentContext === undefined) {
    currentContext = new binding.NJSItfExecutionContext(NJSContextImpl)
    NJSThreadDispatcherImpl.contexts[name] = currentContext
  }
  return currentContext
}

NJSThreadDispatcherImpl.getThreadPoolExecutionContext = name => {
  return NJSThreadDispatcherImpl.getSerialExecutionContext(name)
}

NJSThreadDispatcherImpl.getThreadPoolExecutionContext = name => {
  return NJSThreadDispatcherImpl.getMainExecutionContext(name)
}

NJSThreadDispatcherImpl.newLock = () => {
  console.log('Not implemented')
}

const NJSTransactionListVmObserverImpl = {}
NJSTransactionListVmObserverImpl.on_update = newData => {
  const countOfNewData = newData.count()
  if (newData.count() > 0) {
    for (var i = 0; i < newData.count(); i++) {
      const tx = newData.getTransaction(i)
    }
  }
}

const makeApi = () => {
  const LGHttp = new binding.NJSItfHttp(NJSHttpImpl)
  const LGThreadDispatcher = new binding.NJSItfThreadDispatcher(NJSThreadDispatcherImpl)
  return new binding.NJSItfApi(LGHttp, LGThreadDispatcher)
}

// -----------------------------------------------------------------------------
//                                  EXPORTS
// -----------------------------------------------------------------------------

exports.getTransactions = function getTransactions(addresses, currency) {
  const api = makeApi()
  const observer = new binding.NJSItfTransactionListVmObserver(NJSTransactionListVmObserverImpl)
  const handle = api.observer_transaction_list()

  return new Promise((resolve, reject) => {
    const NJSItfHandleResponseImpl = {}

    NJSItfHandleResponseImpl.respond = response => {
      if (response.error) {
        return reject(response.error)
      }
      console.log(JSON.parse(response.result))
      resolve(response.result)
    }

    handle.start(
      observer,
      addresses,
      currency,
      new binding.NJSItfHandleResponse(NJSItfHandleResponseImpl),
    )
  })
}
