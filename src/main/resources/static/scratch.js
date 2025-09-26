db.lead.aggregate([
    {
        $lookup:
            {
                from: "users",
                let: {cid: "$assignedToId"},
                pipeline: [
                    {$match: {$expr: {$eq: ["$_id", "$$cid"]}}},
                    {$project: {username: 1, _id: 0}}
                ],
                as: "user"
            }
    }
])