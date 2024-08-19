package contracts

import org.springframework.cloud.contract.spec.Contract;

Contract.make {
    request {
        method POST()
        url '/api/payment/status'
        body('''
                {
                  "TerminalKey": "TinkoffBankTest",
                  "Amount": 100000,
                  "OrderId": "21050",
                  "Success": true,
                  "Status": "string",
                  "PaymentId": "13660",
                  "ErrorCode": "0",
                  "Message": "string",
                  "Details": "string",
                  "RebillId": 3207469334,
                  "CardId": 10452089,
                  "Pan": "string",
                  "ExpDate": "0229",
                  "Token": "7241ac8307f349afb7bb9dda760717721bbb45950b97c67289f23d8c69cc7b96",
                  "DATA": {
                    "Route": "TCB",
                    "Source": "Installment",
                    "CreditAmount": 10000
                  }
                }
            '''
        )
    }

    response {
        status OK()
        body('OK')
    }
}
